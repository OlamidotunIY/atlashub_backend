package com.atlashub.analytics.application.query.GetSalesSummary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesSummaryResult(
        LocalDate date,
        int totalTransactions,
        BigDecimal totalGross,
        BigDecimal totalDiscounts,
        BigDecimal totalTax,
        BigDecimal totalNet,
        BigDecimal cashSales,
        BigDecimal cardSales,
        BigDecimal creditSales
) {}
