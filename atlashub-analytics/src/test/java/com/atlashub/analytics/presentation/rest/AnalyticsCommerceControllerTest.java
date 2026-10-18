package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetCashierLeaderboard.CashierPerformanceResult;
import com.atlashub.analytics.application.query.GetCashierLeaderboard.GetCashierLeaderboardHandler;
import com.atlashub.analytics.application.query.GetCashierLeaderboard.GetCashierLeaderboardQuery;
import com.atlashub.analytics.application.query.GetInventoryValue.GetInventoryValueHandler;
import com.atlashub.analytics.application.query.GetInventoryValue.GetInventoryValueQuery;
import com.atlashub.analytics.application.query.GetInventoryValue.InventoryValueResult;
import com.atlashub.analytics.application.query.GetSalesSummary.DailySalesSummaryResult;
import com.atlashub.analytics.application.query.GetSalesSummary.GetSalesSummaryHandler;
import com.atlashub.analytics.application.query.GetSalesSummary.GetSalesSummaryQuery;
import com.atlashub.analytics.application.query.GetTopSellingProducts.GetTopSellingProductsHandler;
import com.atlashub.analytics.application.query.GetTopSellingProducts.GetTopSellingProductsQuery;
import com.atlashub.analytics.application.query.GetTopSellingProducts.TopProductResult;
import com.atlashub.analytics.presentation.dto.CashierPerformanceDto;
import com.atlashub.analytics.presentation.dto.DailySalesSummaryDto;
import com.atlashub.analytics.presentation.dto.InventoryValueDto;
import com.atlashub.analytics.presentation.dto.TopProductDto;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsCommerceControllerTest {

    @Mock
    private GetSalesSummaryHandler salesSummaryHandler;
    @Mock
    private GetTopSellingProductsHandler topSellingProductsHandler;
    @Mock
    private GetCashierLeaderboardHandler cashierLeaderboardHandler;
    @Mock
    private GetInventoryValueHandler inventoryValueHandler;

    private AnalyticsCommerceController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsCommerceController(
                salesSummaryHandler,
                topSellingProductsHandler,
                cashierLeaderboardHandler,
                inventoryValueHandler
        );
        principal = new AuthenticatedPrincipal(1L, 100L, "live", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should return 200 with daily sales summary")
    void shouldReturnSalesSummary() {
        DailySalesSummaryResult result = new DailySalesSummaryResult(
                LocalDate.of(2026, 10, 15),
                5,
                BigDecimal.valueOf(5000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(salesSummaryHandler.execute(any(GetSalesSummaryQuery.class)))
                .thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<DailySalesSummaryDto>>> response =
                controller.getSalesSummary(principal, 200L, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());
        assertEquals(BigDecimal.valueOf(5000), response.getBody().data().get(0).totalGross());
    }

    @Test
    @DisplayName("Should return 200 with top selling products")
    void shouldReturnTopProducts() {
        TopProductResult result = new TopProductResult(401L, 25, BigDecimal.valueOf(50000), "2026-10");
        when(topSellingProductsHandler.execute(any(GetTopSellingProductsQuery.class)))
                .thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<TopProductDto>>> response =
                controller.getTopSellingProducts(principal, "2026-10", 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals(401L, response.getBody().data().get(0).productId());
    }

    @Test
    @DisplayName("Should return 200 with cashier leaderboard")
    void shouldReturnCashierLeaderboard() {
        CashierPerformanceResult result = new CashierPerformanceResult(
                12L, LocalDate.now(), 10, BigDecimal.valueOf(15000), BigDecimal.valueOf(1500)
        );
        when(cashierLeaderboardHandler.execute(any(GetCashierLeaderboardQuery.class)))
                .thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<CashierPerformanceDto>>> response =
                controller.getCashierLeaderboard(principal, LocalDate.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals(12L, response.getBody().data().get(0).cashierId());
    }

    @Test
    @DisplayName("Should return 200 with inventory value")
    void shouldReturnInventoryValue() {
        InventoryValueResult result = new InventoryValueResult(200L, 50, 500, BigDecimal.valueOf(1200000), 3);
        when(inventoryValueHandler.execute(any(GetInventoryValueQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<InventoryValueDto>> response =
                controller.getInventoryValue(principal, 200L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().data().totalSkus());
        assertEquals(500, response.getBody().data().totalStock());
        assertEquals(BigDecimal.valueOf(1200000), response.getBody().data().totalStockValue());
    }
}
