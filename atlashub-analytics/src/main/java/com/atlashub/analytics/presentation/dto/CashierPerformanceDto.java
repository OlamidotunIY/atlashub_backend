package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashierPerformanceDto(
        Long cashierId,
        LocalDate date,
        int totalTransactions,
        BigDecimal totalRevenue,
        BigDecimal averageTransactionValue
) {}
