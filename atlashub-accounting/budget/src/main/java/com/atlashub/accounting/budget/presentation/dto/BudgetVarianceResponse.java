package com.atlashub.accounting.budget.presentation.dto;

import java.math.BigDecimal;

public record BudgetVarianceResponse(
        Long budgetId,
        Long accountId,
        String period,
        BigDecimal budgetedAmount,
        BigDecimal actualAmount,
        BigDecimal variance,
        BigDecimal variancePercentage,
        String currency
) {}
