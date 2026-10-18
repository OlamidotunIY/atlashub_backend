package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.BusinessFeatureSnapshotResult;
import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.GetBusinessFeatureSnapshotHandler;
import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.GetBusinessFeatureSnapshotQuery;
import com.atlashub.analytics.application.query.GetBusinessPerformance.BusinessPerformanceResult;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceHandler;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceQuery;
import com.atlashub.analytics.application.query.GetFinancialHealth.FinancialHealthResult;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthHandler;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthQuery;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.FinancingBehaviourResult;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.GetFinancingBehaviourHandler;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.GetFinancingBehaviourQuery;
import com.atlashub.analytics.application.query.GetInventoryHealth.GetInventoryHealthHandler;
import com.atlashub.analytics.application.query.GetInventoryHealth.GetInventoryHealthQuery;
import com.atlashub.analytics.application.query.GetInventoryHealth.InventoryHealthResult;
import com.atlashub.analytics.application.query.GetOutletPerformance.GetOutletPerformanceHandler;
import com.atlashub.analytics.application.query.GetOutletPerformance.GetOutletPerformanceQuery;
import com.atlashub.analytics.application.query.GetOutletPerformance.OutletPerformanceResult;
import com.atlashub.analytics.presentation.dto.BusinessFeatureSnapshotDto;
import com.atlashub.analytics.presentation.dto.BusinessPerformanceDto;
import com.atlashub.analytics.presentation.dto.FinancialHealthDto;
import com.atlashub.analytics.presentation.dto.FinancingBehaviourDto;
import com.atlashub.analytics.presentation.dto.InventoryHealthDto;
import com.atlashub.analytics.presentation.dto.OutletPerformanceDto;
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
@RequestMapping("/api/v1/analytics/intelligence")
@Tag(name = "Analytics - Intelligence", description = "Endpoints for AI/ML feature snapshots, financial health, and projections")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsIntelligenceController {

    private final GetBusinessPerformanceHandler businessPerformanceHandler;
    private final GetFinancialHealthHandler financialHealthHandler;
    private final GetInventoryHealthHandler inventoryHealthHandler;
    private final GetOutletPerformanceHandler outletPerformanceHandler;
    private final GetFinancingBehaviourHandler financingBehaviourHandler;
    private final GetBusinessFeatureSnapshotHandler businessFeatureSnapshotHandler;

    public AnalyticsIntelligenceController(
            GetBusinessPerformanceHandler businessPerformanceHandler,
            GetFinancialHealthHandler financialHealthHandler,
            GetInventoryHealthHandler inventoryHealthHandler,
            GetOutletPerformanceHandler outletPerformanceHandler,
            GetFinancingBehaviourHandler financingBehaviourHandler,
            GetBusinessFeatureSnapshotHandler businessFeatureSnapshotHandler
    ) {
        this.businessPerformanceHandler = businessPerformanceHandler;
        this.financialHealthHandler = financialHealthHandler;
        this.inventoryHealthHandler = inventoryHealthHandler;
        this.outletPerformanceHandler = outletPerformanceHandler;
        this.financingBehaviourHandler = financingBehaviourHandler;
        this.businessFeatureSnapshotHandler = businessFeatureSnapshotHandler;
    }

    @GetMapping("/performance")
    @Operation(summary = "Get overall business performance", description = "Returns aggregated performance metrics for scoring and intelligence")
    public ResponseEntity<ApiResponse<BusinessPerformanceDto>> getBusinessPerformance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        LocalDate date = asOfDate != null ? asOfDate : LocalDate.now();

        BusinessPerformanceResult result = businessPerformanceHandler.execute(
                new GetBusinessPerformanceQuery(principal.activeOrganizationId(), principal.environment(), date)
        );

        BusinessPerformanceDto dto = new BusinessPerformanceDto(
                result.organizationId(),
                result.environment(),
                result.asOfDate(),
                result.totalGrossRevenue(),
                result.totalNetRevenue(),
                result.totalTransactions(),
                result.averageOrderValue(),
                result.repeatCustomerRate(),
                result.activeOutletsCount()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Business performance retrieved successfully", dto, null));
    }

    @GetMapping("/financial-health")
    @Operation(summary = "Get financial health", description = "Returns accounting-backed P&L and cash-flow health metrics")
    public ResponseEntity<ApiResponse<FinancialHealthDto>> getFinancialHealth(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd
    ) {
        LocalDate date = periodEnd != null ? periodEnd : LocalDate.now();

        FinancialHealthResult result = financialHealthHandler.execute(
                new GetFinancialHealthQuery(principal.activeOrganizationId(), principal.environment(), date)
        );

        FinancialHealthDto dto = new FinancialHealthDto(
                result.organizationId(),
                result.environment(),
                result.periodEnd(),
                result.revenue(),
                result.costOfSales(),
                result.grossProfit(),
                result.grossMargin(),
                result.operatingExpense(),
                result.netOperatingIncome(),
                result.cashBalance(),
                result.accountsReceivable(),
                result.accountsPayable(),
                result.reconciliationStatus()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Financial health retrieved successfully", dto, null));
    }

    @GetMapping("/inventory-health")
    @Operation(summary = "Get inventory health", description = "Returns stock coverage, dead stock, and restocking projections")
    public ResponseEntity<ApiResponse<List<InventoryHealthDto>>> getInventoryHealth(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long outletId
    ) {
        List<InventoryHealthResult> results = inventoryHealthHandler.execute(
                new GetInventoryHealthQuery(principal.activeOrganizationId(), principal.environment(), outletId)
        );

        List<InventoryHealthDto> dtos = results.stream()
                .map(r -> new InventoryHealthDto(
                        r.productId(),
                        r.outletId(),
                        r.currentStock(),
                        r.reorderPoint(),
                        r.stockCoverageDays(),
                        r.inventoryValue(),
                        r.deadStock()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory health retrieved successfully", dtos, null));
    }

    @GetMapping("/outlet-performance")
    @Operation(summary = "Get outlet performance", description = "Returns comparative performance across outlets")
    public ResponseEntity<ApiResponse<List<OutletPerformanceDto>>> getOutletPerformance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String month
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        List<OutletPerformanceResult> results = outletPerformanceHandler.execute(
                new GetOutletPerformanceQuery(principal.activeOrganizationId(), principal.environment(), m)
        );

        List<OutletPerformanceDto> dtos = results.stream()
                .map(r -> new OutletPerformanceDto(
                        r.outletId(),
                        r.month(),
                        r.totalSales(),
                        r.totalDiscounts(),
                        r.totalExpenses(),
                        r.grossMargin(),
                        r.netProfit(),
                        r.transactionCount()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Outlet performance retrieved successfully", dtos, null));
    }

    @GetMapping("/financing-behaviour")
    @Operation(summary = "Get financing behaviour", description = "Returns payment baseline, repayment rates, and anomalies")
    public ResponseEntity<ApiResponse<FinancingBehaviourDto>> getFinancingBehaviour(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(defaultValue = "DEFAULT") String paymentContextKey
    ) {
        FinancingBehaviourResult result = financingBehaviourHandler.execute(
                new GetFinancingBehaviourQuery(principal.activeOrganizationId(), principal.environment(), paymentContextKey)
        );

        FinancingBehaviourDto dto = new FinancingBehaviourDto(
                result.organizationId(),
                result.environment(),
                result.paymentContextKey(),
                result.totalInflow(),
                result.totalOutflow(),
                result.averageDailyBalance(),
                result.repaymentSuccessRate(),
                result.anomalyCount()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Financing behaviour retrieved successfully", dto, null));
    }

    @GetMapping("/feature-snapshots")
    @Operation(summary = "Get business feature snapshot", description = "Returns immutable feature bundle by snapshot ID or latest")
    public ResponseEntity<ApiResponse<BusinessFeatureSnapshotDto>> getFeatureSnapshot(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) Long snapshotId,
            @RequestParam(defaultValue = "v1.0") String schemaVersion
    ) {
        BusinessFeatureSnapshotResult result = businessFeatureSnapshotHandler.execute(
                new GetBusinessFeatureSnapshotQuery(snapshotId, principal.activeOrganizationId(), principal.environment(), schemaVersion)
        );

        BusinessFeatureSnapshotDto dto = new BusinessFeatureSnapshotDto(
                result.snapshotId(),
                result.organizationId(),
                result.environment(),
                result.schemaVersion(),
                result.windowStart(),
                result.windowEnd(),
                result.asOf(),
                result.completeness(),
                result.metricValues(),
                result.sourceProjectionHashes(),
                result.generatedAt(),
                result.expiresAt()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Feature snapshot retrieved successfully", dto, null));
    }
}
