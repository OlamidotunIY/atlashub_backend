package com.atlashub.analytics.application.query.GetWalletOverview;

import java.math.BigDecimal;

public record WalletBalanceSummaryResult(
        BigDecimal operatingBalance,
        BigDecimal escrowBalance,
        BigDecimal payrollReserveBalance,
        BigDecimal taxHoldingBalance
) {}
