package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record WalletBalanceSummaryDto(
        BigDecimal operatingBalance,
        BigDecimal escrowBalance,
        BigDecimal payrollReserveBalance,
        BigDecimal taxHoldingBalance
) {}
