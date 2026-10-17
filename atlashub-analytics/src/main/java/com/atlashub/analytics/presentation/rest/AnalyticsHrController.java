package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetHeadcountSummary.GetHeadcountSummaryHandler;
import com.atlashub.analytics.application.query.GetHeadcountSummary.GetHeadcountSummaryQuery;
import com.atlashub.analytics.application.query.GetHeadcountSummary.HeadcountResult;
import com.atlashub.analytics.application.query.GetPayrollCostTrend.GetPayrollCostTrendHandler;
import com.atlashub.analytics.application.query.GetPayrollCostTrend.GetPayrollCostTrendQuery;
import com.atlashub.analytics.application.query.GetPayrollCostTrend.MonthlyPayrollCostResult;
import com.atlashub.analytics.presentation.dto.HeadcountDto;
import com.atlashub.analytics.presentation.dto.MonthlyPayrollCostDto;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/analytics/hr")
@Tag(name = "Analytics - HR", description = "Endpoints for payroll and workforce analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsHrController {

    private final GetPayrollCostTrendHandler payrollCostTrendHandler;
    private final GetHeadcountSummaryHandler headcountSummaryHandler;

    public AnalyticsHrController(
            GetPayrollCostTrendHandler payrollCostTrendHandler,
            GetHeadcountSummaryHandler headcountSummaryHandler
    ) {
        this.payrollCostTrendHandler = payrollCostTrendHandler;
        this.headcountSummaryHandler = headcountSummaryHandler;
    }

    @GetMapping("/payroll-trend")
    @Operation(summary = "Get payroll cost trend", description = "Returns monthly payroll cost trends for a year")
    public ResponseEntity<ApiResponse<List<MonthlyPayrollCostDto>>> getPayrollCostTrend(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String year
    ) {
        String y = (year != null && !year.isBlank()) ? year : String.valueOf(LocalDate.now().getYear());

        List<MonthlyPayrollCostResult> results = payrollCostTrendHandler.execute(
                new GetPayrollCostTrendQuery(principal.activeOrganizationId(), principal.environment(), y)
        );

        List<MonthlyPayrollCostDto> dtos = results.stream()
                .map(r -> new MonthlyPayrollCostDto(
                        r.month(),
                        r.totalGrossPayroll(),
                        r.totalDeductions(),
                        r.totalNetPayroll(),
                        r.headcount()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Payroll cost trend retrieved successfully", dtos, null));
    }

    @GetMapping("/headcount")
    @Operation(summary = "Get headcount summary", description = "Returns current workforce distribution")
    public ResponseEntity<ApiResponse<HeadcountDto>> getHeadcount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        HeadcountResult result = headcountSummaryHandler.execute(
                new GetHeadcountSummaryQuery(principal.activeOrganizationId(), principal.environment())
        );

        HeadcountDto dto = new HeadcountDto(
                result.total(),
                result.active(),
                result.onLeave(),
                result.suspended(),
                result.terminated(),
                result.newHiresThisMonth()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Headcount summary retrieved successfully", dto, null));
    }
}
