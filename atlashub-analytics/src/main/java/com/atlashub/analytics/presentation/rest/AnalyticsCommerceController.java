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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics - Commerce", description = "Endpoints for sales and inventory analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsCommerceController {

    private final GetSalesSummaryHandler salesSummaryHandler;
    private final GetTopSellingProductsHandler topSellingProductsHandler;
    private final GetCashierLeaderboardHandler cashierLeaderboardHandler;
    private final GetInventoryValueHandler inventoryValueHandler;

    public AnalyticsCommerceController(
            GetSalesSummaryHandler salesSummaryHandler,
            GetTopSellingProductsHandler topSellingProductsHandler,
            GetCashierLeaderboardHandler cashierLeaderboardHandler,
            GetInventoryValueHandler inventoryValueHandler
    ) {
        this.salesSummaryHandler = salesSummaryHandler;
        this.topSellingProductsHandler = topSellingProductsHandler;
        this.cashierLeaderboardHandler = cashierLeaderboardHandler;
        this.inventoryValueHandler = inventoryValueHandler;
    }

    @GetMapping("/sales/summary")
    @Operation(summary = "Get daily sales summary", description = "Returns daily sales aggregates for date range")
    public ResponseEntity<ApiResponse<List<DailySalesSummaryDto>>> getSalesSummary(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        LocalDate to = dateTo != null ? dateTo : LocalDate.now();
        LocalDate from = dateFrom != null ? dateFrom : to.minusDays(30);

        List<DailySalesSummaryResult> results = salesSummaryHandler.execute(
                new GetSalesSummaryQuery(principal.activeOrganizationId(), principal.environment(), outletId, from, to)
        );

        List<DailySalesSummaryDto> dtos = results.stream()
                .map(r -> new DailySalesSummaryDto(
                        r.date(),
                        r.totalTransactions(),
                        r.totalGross(),
                        r.totalDiscounts(),
                        r.totalTax(),
                        r.totalNet(),
                        r.cashSales(),
                        r.cardSales(),
                        r.creditSales()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Sales summary retrieved successfully", dtos, null));
    }

    @GetMapping("/sales/top-products")
    @Operation(summary = "Get top selling products", description = "Returns top products for a month")
    public ResponseEntity<ApiResponse<List<TopProductDto>>> getTopSellingProducts(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        List<TopProductResult> results = topSellingProductsHandler.execute(
                new GetTopSellingProductsQuery(principal.activeOrganizationId(), principal.environment(), m, limit)
        );

        List<TopProductDto> dtos = results.stream()
                .map(r -> new TopProductDto(r.productId(), r.quantitySold(), r.totalRevenue(), r.month()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Top selling products retrieved successfully", dtos, null));
    }

    @GetMapping("/sales/cashier-leaderboard")
    @Operation(summary = "Get cashier leaderboard", description = "Returns cashier performance metrics for a specific date")
    public ResponseEntity<ApiResponse<List<CashierPerformanceDto>>> getCashierLeaderboard(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate d = date != null ? date : LocalDate.now();

        List<CashierPerformanceResult> results = cashierLeaderboardHandler.execute(
                new GetCashierLeaderboardQuery(principal.activeOrganizationId(), principal.environment(), d)
        );

        List<CashierPerformanceDto> dtos = results.stream()
                .map(r -> new CashierPerformanceDto(
                        r.cashierId(),
                        r.date(),
                        r.totalTransactions(),
                        r.totalRevenue(),
                        r.averageTransactionValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Cashier leaderboard retrieved successfully", dtos, null));
    }

    @GetMapping("/inventory/value")
    @Operation(summary = "Get inventory valuation", description = "Returns current stock valuation and SKU metrics")
    public ResponseEntity<ApiResponse<InventoryValueDto>> getInventoryValue(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long outletId
    ) {
        InventoryValueResult result = inventoryValueHandler.execute(
                new GetInventoryValueQuery(principal.activeOrganizationId(), principal.environment(), outletId)
        );

        InventoryValueDto dto = new InventoryValueDto(
                result.outletId(),
                result.totalSkus(),
                result.totalStock(),
                result.totalStockValue(),
                result.lowStockCount()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory value retrieved successfully", dto, null));
    }
}
