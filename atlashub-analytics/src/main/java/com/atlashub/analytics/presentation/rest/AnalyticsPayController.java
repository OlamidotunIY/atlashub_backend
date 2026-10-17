package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetTransactionVolume.GetTransactionVolumeHandler;
import com.atlashub.analytics.application.query.GetTransactionVolume.GetTransactionVolumeQuery;
import com.atlashub.analytics.application.query.GetTransactionVolume.TransactionVolumeResult;
import com.atlashub.analytics.application.query.GetWalletOverview.GetWalletOverviewHandler;
import com.atlashub.analytics.application.query.GetWalletOverview.GetWalletOverviewQuery;
import com.atlashub.analytics.application.query.GetWalletOverview.WalletBalanceSummaryResult;
import com.atlashub.analytics.presentation.dto.TransactionVolumeDto;
import com.atlashub.analytics.presentation.dto.WalletBalanceSummaryDto;
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

@RestController
@RequestMapping("/api/v1/analytics/pay")
@Tag(name = "Analytics - Pay", description = "Endpoints for payment and ledger analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsPayController {

    private final GetTransactionVolumeHandler transactionVolumeHandler;
    private final GetWalletOverviewHandler walletOverviewHandler;

    public AnalyticsPayController(
            GetTransactionVolumeHandler transactionVolumeHandler,
            GetWalletOverviewHandler walletOverviewHandler
    ) {
        this.transactionVolumeHandler = transactionVolumeHandler;
        this.walletOverviewHandler = walletOverviewHandler;
    }

    @GetMapping("/transaction-volume")
    @Operation(summary = "Get transaction volume", description = "Returns charge and payout volume metrics for a month")
    public ResponseEntity<ApiResponse<TransactionVolumeDto>> getTransactionVolume(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String month
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        TransactionVolumeResult result = transactionVolumeHandler.execute(
                new GetTransactionVolumeQuery(principal.activeOrganizationId(), principal.environment(), m)
        );

        TransactionVolumeDto dto = new TransactionVolumeDto(
                result.month(),
                result.totalCharges(),
                result.totalVolume(),
                result.successfulCharges(),
                result.failedCharges(),
                result.totalPayouts(),
                result.totalPayoutAmount(),
                result.platformFeeCollected()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Transaction volume retrieved successfully", dto, null));
    }

    @GetMapping("/wallet-overview")
    @Operation(summary = "Get wallet overview", description = "Returns current balances for operating, escrow, payroll and tax accounts")
    public ResponseEntity<ApiResponse<WalletBalanceSummaryDto>> getWalletOverview(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        WalletBalanceSummaryResult result = walletOverviewHandler.execute(
                new GetWalletOverviewQuery(principal.activeOrganizationId(), principal.environment())
        );

        WalletBalanceSummaryDto dto = new WalletBalanceSummaryDto(
                result.operatingBalance(),
                result.escrowBalance(),
                result.payrollReserveBalance(),
                result.taxHoldingBalance()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Wallet overview retrieved successfully", dto, null));
    }
}
