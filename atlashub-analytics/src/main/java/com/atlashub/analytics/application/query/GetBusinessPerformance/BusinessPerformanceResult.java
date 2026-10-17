package com.atlashub.analytics.application.query.GetBusinessPerformance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BusinessPerformanceResult(
        Long organizationId,
        String environment,
        LocalDate asOfDate,
        BigDecimal totalGrossRevenue,
        BigDecimal totalNetRevenue,
        long totalTransactions,
        BigDecimal averageOrderValue,
        BigDecimal repeatCustomerRate,
        BigDecimal activeOutletsCount
) {}
