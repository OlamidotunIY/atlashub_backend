package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesSummaryDto(
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
