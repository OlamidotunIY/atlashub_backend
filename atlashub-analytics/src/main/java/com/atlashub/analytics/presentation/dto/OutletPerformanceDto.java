package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record OutletPerformanceDto(
        Long outletId,
        String month,
        BigDecimal totalSales,
        BigDecimal totalDiscounts,
        BigDecimal totalExpenses,
        BigDecimal grossMargin,
        BigDecimal netProfit,
        int transactionCount
) {}
