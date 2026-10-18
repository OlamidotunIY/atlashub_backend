package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetPlatformFeeRevenue.GetPlatformFeeRevenueHandler;
import com.atlashub.analytics.application.query.GetPlatformFeeRevenue.GetPlatformFeeRevenueQuery;
import com.atlashub.analytics.application.query.GetPlatformFeeRevenue.PlatformFeeResult;
import com.atlashub.analytics.application.query.GetPlatformTransactionVolume.GetPlatformTransactionVolumeHandler;
import com.atlashub.analytics.application.query.GetPlatformTransactionVolume.GetPlatformTransactionVolumeQuery;
import com.atlashub.analytics.application.query.GetPlatformTransactionVolume.PlatformVolumeResult;
import com.atlashub.analytics.presentation.dto.PlatformFeeDto;
import com.atlashub.analytics.presentation.dto.PlatformVolumeDto;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics/platform")
@Tag(name = "Analytics - Platform Admin", description = "Cross-organization admin metrics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsPlatformController {

    private final GetPlatformTransactionVolumeHandler platformTransactionVolumeHandler;
    private final GetPlatformFeeRevenueHandler platformFeeRevenueHandler;

    public AnalyticsPlatformController(
            GetPlatformTransactionVolumeHandler platformTransactionVolumeHandler,
            GetPlatformFeeRevenueHandler platformFeeRevenueHandler
    ) {
        this.platformTransactionVolumeHandler = platformTransactionVolumeHandler;
        this.platformFeeRevenueHandler = platformFeeRevenueHandler;
    }

    @GetMapping("/transaction-volume")
    @PreAuthorize("hasAuthority('analytics:platform:read')")
    @Operation(summary = "Get platform transaction volume", description = "Returns system-wide transaction volume across all organizations")
    public ResponseEntity<ApiResponse<PlatformVolumeDto>> getPlatformTransactionVolume(
            @RequestParam(required = false) String month
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        PlatformVolumeResult result = platformTransactionVolumeHandler.execute(
                new GetPlatformTransactionVolumeQuery(m)
        );

        PlatformVolumeDto dto = new PlatformVolumeDto(
                result.month(),
                result.totalCharges(),
                result.totalVolume(),
                result.successfulCharges(),
                result.failedCharges(),
                result.totalPayouts(),
                result.totalPayoutAmount(),
                result.totalPlatformFees()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Platform transaction volume retrieved successfully", dto, null));
    }

    @GetMapping("/fee-revenue")
    @PreAuthorize("hasAuthority('analytics:platform:read')")
    @Operation(summary = "Get platform fee revenue", description = "Returns system-wide platform fee collections for a month")
    public ResponseEntity<ApiResponse<PlatformFeeDto>> getPlatformFeeRevenue(
            @RequestParam(required = false) String month
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        PlatformFeeResult result = platformFeeRevenueHandler.execute(
                new GetPlatformFeeRevenueQuery(m)
        );

        PlatformFeeDto dto = new PlatformFeeDto(result.month(), result.totalPlatformFees());
        return ResponseEntity.ok(new ApiResponse<>(true, "Platform fee revenue retrieved successfully", dto, null));
    }
}
