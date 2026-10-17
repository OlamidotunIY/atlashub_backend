package com.atlashub.analytics.application.query.GetCashierLeaderboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashierPerformanceResult(
        Long cashierId,
        LocalDate date,
        int totalTransactions,
        BigDecimal totalRevenue,
        BigDecimal averageTransactionValue
) {}
