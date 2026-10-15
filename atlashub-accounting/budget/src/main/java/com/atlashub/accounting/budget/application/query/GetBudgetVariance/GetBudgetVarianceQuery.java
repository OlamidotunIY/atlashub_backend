package com.atlashub.accounting.budget.application.query.GetBudgetVariance;

public record GetBudgetVarianceQuery(
        Long budgetId,
        Long organizationId
) {}
