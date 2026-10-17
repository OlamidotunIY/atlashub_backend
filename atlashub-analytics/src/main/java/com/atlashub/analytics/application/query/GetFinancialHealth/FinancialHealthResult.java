package com.atlashub.analytics.application.query.GetFinancialHealth;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialHealthResult(
        Long organizationId,
        String environment,
        LocalDate periodEnd,
        BigDecimal revenue,
        BigDecimal costOfSales,
        BigDecimal grossProfit,
        BigDecimal grossMargin,
        BigDecimal operatingExpense,
        BigDecimal netOperatingIncome,
        BigDecimal cashBalance,
        BigDecimal accountsReceivable,
        BigDecimal accountsPayable,
        String reconciliationStatus
) {}
