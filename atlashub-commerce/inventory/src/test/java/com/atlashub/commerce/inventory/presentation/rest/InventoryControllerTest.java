package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.AdjustStock.AdjustStockCommand;
import com.atlashub.commerce.inventory.application.commands.AdjustStock.AdjustStockHandler;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.GetInventoryLevelHandler;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.GetInventoryLevelQuery;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.InventoryResult;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.GetStockSummaryHandler;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.GetStockSummaryQuery;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.StockSummaryResult;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.ListLowStockProductsHandler;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.ListLowStockProductsQuery;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.LowStockResult;
import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.commerce.inventory.presentation.dto.AdjustStockRequest;
import com.atlashub.commerce.inventory.presentation.dto.InventoryLevelResponse;
import com.atlashub.commerce.inventory.presentation.dto.LowStockResponse;
import com.atlashub.commerce.inventory.presentation.dto.StockSummaryResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private GetInventoryLevelHandler getInventoryLevelHandler;

    @Mock
    private GetStockSummaryHandler getStockSummaryHandler;

    @Mock
    private ListLowStockProductsHandler listLowStockProductsHandler;

    @Mock
    private AdjustStockHandler adjustStockHandler;

    @InjectMocks
    private InventoryController controller;

    private AuthenticatedPrincipal createPrincipal() {
        return new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-123",
                "tok-123",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should return inventory level for outlet and product")
    void getInventoryLevel_shouldReturn200AndLevel() {
        AuthenticatedPrincipal principal = createPrincipal();
        InventoryResult result = new InventoryResult(1L, 10L, 20L, 100L, null, 10, 2, 8, 5, 2, false);
        when(getInventoryLevelHandler.execute(any(GetInventoryLevelQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<InventoryLevelResponse>> response =
                controller.getInventoryLevel(principal, 20L, 100L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(10, response.getBody().data().quantity());
        assertEquals(8, response.getBody().data().availableQuantity());
    }

    @Test
    @DisplayName("Should return stock summary metrics")
    void getStockSummary_shouldReturn200AndSummary() {
        AuthenticatedPrincipal principal = createPrincipal();
        StockSummaryResult result = new StockSummaryResult(20L, 50, 200, 3, 1);
        when(getStockSummaryHandler.execute(any(GetStockSummaryQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<StockSummaryResponse>> response = controller.getStockSummary(principal, 20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(50, response.getBody().data().totalSkus());
        assertEquals(200, response.getBody().data().totalQuantity());
    }

    @Test
    @DisplayName("Should list low stock products")
    void listLowStockProducts_shouldReturn200AndList() {
        AuthenticatedPrincipal principal = createPrincipal();
        List<LowStockResult> results = List.of(
                new LowStockResult(1L, 10L, 20L, 100L, null, 2, 5, 2)
        );
        when(listLowStockProductsHandler.execute(any(ListLowStockProductsQuery.class))).thenReturn(results);

        ResponseEntity<ApiResponse<List<LowStockResponse>>> response = controller.listLowStockProducts(principal, 20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
        assertEquals(100L, response.getBody().data().getFirst().productId());
    }

    @Test
    @DisplayName("Should adjust stock and return 200")
    void adjustStock_shouldReturn200AndInvokeHandler() {
        AuthenticatedPrincipal principal = createPrincipal();
        AdjustStockRequest request = new AdjustStockRequest(1L, 15, AdjustmentReason.CORRECTION);

        ResponseEntity<ApiResponse<Void>> response = controller.adjustStock(principal, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<AdjustStockCommand> captor = ArgumentCaptor.forClass(AdjustStockCommand.class);
        verify(adjustStockHandler).execute(captor.capture());
        assertEquals(1L, captor.getValue().inventoryId());
        assertEquals(15, captor.getValue().newQuantity());
        assertEquals(AdjustmentReason.CORRECTION, captor.getValue().reason());
        assertEquals(1L, captor.getValue().adjustedBy());
    }
}
