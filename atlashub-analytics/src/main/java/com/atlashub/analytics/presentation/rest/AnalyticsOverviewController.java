package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetBusinessOverview.BusinessOverviewResult;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewHandler;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewQuery;
import com.atlashub.analytics.presentation.dto.BusinessOverviewDto;
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
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/analytics/overview")
@Tag(name = "Analytics - Overview", description = "Cross-module business overview projections")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsOverviewController {
    private final GetBusinessOverviewHandler handler;
    public AnalyticsOverviewController(GetBusinessOverviewHandler handler) { this.handler = handler; }

    @GetMapping
    @Operation(summary = "Get business overview", description = "Returns organization-scoped, eventually-consistent analytics projections")
    public ResponseEntity<ApiResponse<BusinessOverviewDto>> getOverview(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        BusinessPerformanceMapper mapper = new BusinessPerformanceMapper(handler.execute(new GetBusinessOverviewQuery(
                principal.activeOrganizationId(), principal.environment().toLowerCase(Locale.ROOT), asOfDate)));
        return ResponseEntity.ok(new ApiResponse<>(true, "Business overview retrieved successfully", mapper.toDto(), null));
    }

    private record BusinessPerformanceMapper(BusinessOverviewResult result) {
        BusinessOverviewDto toDto() {
            return new BusinessOverviewDto(result.fromDate(), result.toDate(),
                    new BusinessOverviewDto.Sales(result.sales().netRevenue(), result.sales().transactions(), result.sales().averageOrderValue()),
                    new BusinessOverviewDto.Payments(result.payments().chargeVolume(), result.payments().successfulCharges(), result.payments().failedCharges(), result.payments().payoutAmount()),
                    new BusinessOverviewDto.People(result.people().total(), result.people().active(), result.people().suspended()),
                    new BusinessOverviewDto.Payroll(result.payroll().gross(), result.payroll().deductions(), result.payroll().net(), result.payroll().headcount()),
                    new BusinessOverviewDto.Commerce(result.commerce().outletCount(), result.commerce().outletSales(), result.commerce().discounts(), result.commerce().expenses(), result.commerce().netProfit(), result.commerce().trackedProducts(), result.commerce().unitsSold(), result.commerce().productRevenue()),
                    new BusinessOverviewDto.Logistics(result.logistics().shipments(), result.logistics().delivered(), result.logistics().failed(), result.logistics().deliverySuccessRate()),
                    new BusinessOverviewDto.Inventory(result.inventory().outOfStockCount(), result.inventory().lowStockCount(), result.inventory().deadStockCount(), result.inventory().inventoryValue()),
                    new BusinessOverviewDto.Finance(result.finance().available(), result.finance().cashBalance(), result.finance().receivables(), result.finance().payables(), result.finance().reconciliationStatus()),
                    result.attention().stream().map(item -> new BusinessOverviewDto.Attention(item.code(), item.module(), item.severity(), item.count())).toList(),
                    result.recentActivity().stream().map(item -> new BusinessOverviewDto.Activity(item.eventId(), item.module(), item.type(), item.amount(), item.currency(), item.occurredAt())).toList());
        }
    }
}
