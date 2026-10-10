package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.ApproveVendorProduct.ApproveVendorProductCommand;
import com.atlashub.commerce.catalog.application.commands.ApproveVendorProduct.ApproveVendorProductHandler;
import com.atlashub.commerce.catalog.application.commands.CreateProduct.CreateProductCommand;
import com.atlashub.commerce.catalog.application.commands.CreateProduct.CreateProductHandler;
import com.atlashub.commerce.catalog.application.commands.CreateProduct.CreateProductResult;
import com.atlashub.commerce.catalog.application.commands.SetProductPrice.SetProductPriceCommand;
import com.atlashub.commerce.catalog.application.commands.SetProductPrice.SetProductPriceHandler;
import com.atlashub.commerce.catalog.application.queries.ListProducts.ListProductsHandler;
import com.atlashub.commerce.catalog.application.queries.ListProducts.ListProductsQuery;
import com.atlashub.commerce.catalog.application.queries.ListProducts.ProductResult;
import com.atlashub.commerce.catalog.application.queries.SearchProductByBarcode.SearchProductByBarcodeHandler;
import com.atlashub.commerce.catalog.application.queries.SearchProductByBarcode.SearchProductByBarcodeQuery;
import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.commerce.catalog.presentation.dto.CreateProductRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateProductResponse;
import com.atlashub.commerce.catalog.presentation.dto.SetProductPriceRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private CreateProductHandler createProductHandler;

    @Mock
    private ListProductsHandler listProductsHandler;

    @Mock
    private SearchProductByBarcodeHandler searchProductByBarcodeHandler;

    @Mock
    private ApproveVendorProductHandler approveVendorProductHandler;

    @Mock
    private SetProductPriceHandler setProductPriceHandler;

    @InjectMocks
    private ProductController controller;

    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-1",
                "tok-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create product and return 201 Created")
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest(
                null,
                "PROD-001",
                "Product One",
                "Desc",
                100L,
                200L,
                true,
                false,
                false,
                ProductType.PHYSICAL
        );

        when(createProductHandler.execute(any(CreateProductCommand.class)))
                .thenReturn(new CreateProductResult(50L));

        ResponseEntity<ApiResponse<CreateProductResponse>> response = controller.createProduct(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(50L, response.getBody().data().productId());

        ArgumentCaptor<CreateProductCommand> captor = ArgumentCaptor.forClass(CreateProductCommand.class);
        verify(createProductHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals("PROD-001", captor.getValue().code());
    }

    @Test
    @DisplayName("Should list products")
    void shouldListProducts() {
        PageResult<ProductResult> page = new PageResult<>(List.of(), 0, 20, 0, 0);
        when(listProductsHandler.execute(any(ListProductsQuery.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResult<ProductResult>>> response =
                controller.listProducts(principal, null, null, ProductStatus.ACTIVE, "test", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(page, response.getBody().data());
    }

    @Test
    @DisplayName("Should search product by barcode")
    void shouldSearchByBarcode() {
        ProductResult result = new ProductResult(
                1L, 10L, null, "BAR123", "Item", "Desc", null, null, null,
                true, false, false, ProductStatus.ACTIVE, ProductType.PHYSICAL, ZonedDateTime.now()
        );
        when(searchProductByBarcodeHandler.execute(any(SearchProductByBarcodeQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ProductResult>> response = controller.searchByBarcode(principal, "BAR123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(result, response.getBody().data());
    }

    @Test
    @DisplayName("Should approve vendor product")
    void shouldApproveVendorProduct() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveVendorProduct(principal, 50L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ApproveVendorProductCommand> captor = ArgumentCaptor.forClass(ApproveVendorProductCommand.class);
        verify(approveVendorProductHandler).execute(captor.capture());
        assertEquals(50L, captor.getValue().productId());
        assertEquals(1L, captor.getValue().approvedByUserId());
    }

    @Test
    @DisplayName("Should set product price")
    void shouldSetProductPrice() {
        SetProductPriceRequest request = new SetProductPriceRequest(
                null,
                PriceLevel.RETAIL,
                new BigDecimal("100.00"),
                new BigDecimal("150.00")
        );

        ResponseEntity<ApiResponse<Void>> response = controller.setProductPrice(50L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<SetProductPriceCommand> captor = ArgumentCaptor.forClass(SetProductPriceCommand.class);
        verify(setProductPriceHandler).execute(captor.capture());
        assertEquals(50L, captor.getValue().productId());
        assertEquals(PriceLevel.RETAIL, captor.getValue().priceLevel());
    }
}
