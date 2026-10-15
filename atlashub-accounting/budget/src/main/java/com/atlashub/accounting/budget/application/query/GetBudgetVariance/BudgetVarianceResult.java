package com.atlashub.accounting.budget.application.query.GetBudgetVariance;

import java.math.BigDecimal;

public record BudgetVarianceResult(
        Long budgetId,
        Long accountId,
        String period,
        BigDecimal budgetedAmount,
        BigDecimal actualAmount,
        BigDecimal variance,
        BigDecimal variancePercentage,
        String currency
) {}
