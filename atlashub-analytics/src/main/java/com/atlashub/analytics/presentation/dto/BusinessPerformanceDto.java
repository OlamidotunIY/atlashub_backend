package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BusinessPerformanceDto(
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
