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
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreateProductRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateProductResponse;
import com.atlashub.commerce.catalog.presentation.dto.SetProductPriceRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final CreateProductHandler createProductHandler;
    private final ListProductsHandler listProductsHandler;
    private final SearchProductByBarcodeHandler searchProductByBarcodeHandler;
    private final ApproveVendorProductHandler approveVendorProductHandler;
    private final SetProductPriceHandler setProductPriceHandler;

    public ProductController(CreateProductHandler createProductHandler, ListProductsHandler listProductsHandler,
                             SearchProductByBarcodeHandler searchProductByBarcodeHandler,
                             ApproveVendorProductHandler approveVendorProductHandler,
                             SetProductPriceHandler setProductPriceHandler) {
        this.createProductHandler =
                Objects.requireNonNull(createProductHandler, "CreateProductHandler must not be null");
        this.listProductsHandler = Objects.requireNonNull(listProductsHandler, "ListProductsHandler must not be null");
        this.searchProductByBarcodeHandler =
                Objects.requireNonNull(searchProductByBarcodeHandler, "SearchProductByBarcodeHandler must not be null");
        this.approveVendorProductHandler =
                Objects.requireNonNull(approveVendorProductHandler, "ApproveVendorProductHandler must not be null");
        this.setProductPriceHandler =
                Objects.requireNonNull(setProductPriceHandler, "SetProductPriceHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command =
                new CreateProductCommand(principal.activeOrganizationId(), request.vendorId(), request.code(),
                        request.name(), request.description(), request.categoryId(), request.departmentId(), null,
                        request.taxable(), request.service(), request.hasVariants(), request.type());
        CreateProductResult result = createProductHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Product created successfully",
                new CreateProductResponse(result.productId()), null));
    }

    @GetMapping
    @Operation(summary = "List products")
    public ResponseEntity<ApiResponse<PageResult<ProductResult>>> listProducts(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long categoryId, @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ListProductsQuery query =
                new ListProductsQuery(principal.activeOrganizationId(), vendorId, categoryId, status, search, page,
                        size);
        PageResult<ProductResult> result = listProductsHandler.execute(query);
        return ok(result);
    }

    @GetMapping("/barcode")
    @Operation(summary = "Search product by barcode")
    public ResponseEntity<ApiResponse<ProductResult>> searchByBarcode(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @RequestParam String barcode) {
        SearchProductByBarcodeQuery query = new SearchProductByBarcodeQuery(principal.activeOrganizationId(), barcode);
        ProductResult result = searchProductByBarcodeHandler.execute(query);
        return ok(result);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve vendor product")
    public ResponseEntity<ApiResponse<Void>> approveVendorProduct(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable Long id) {
        ApproveVendorProductCommand command = new ApproveVendorProductCommand(id, principal.userId());
        approveVendorProductHandler.execute(command);
        return done("Product approved successfully");
    }

    @PostMapping("/{id}/price")
    @Operation(summary = "Set product price")
    public ResponseEntity<ApiResponse<Void>> setProductPrice(@PathVariable Long id,
                                                             @Valid @RequestBody SetProductPriceRequest request) {
        SetProductPriceCommand command = new SetProductPriceCommand(id, request.variantId(), request.priceLevel(),
                request.costPrice() != null ? Money.of(request.costPrice(), CurrencyCode.NGN) : null,
                Money.of(request.sellingPrice(), CurrencyCode.NGN));
        setProductPriceHandler.execute(command);
        return done("Product price set successfully");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
