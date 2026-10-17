package com.atlashub.analytics.application.query.GetOutletPerformance;

import java.math.BigDecimal;

public record OutletPerformanceResult(
        Long outletId,
        String month,
        BigDecimal totalSales,
        BigDecimal totalDiscounts,
        BigDecimal totalExpenses,
        BigDecimal grossMargin,
        BigDecimal netProfit,
        int transactionCount
) {}
