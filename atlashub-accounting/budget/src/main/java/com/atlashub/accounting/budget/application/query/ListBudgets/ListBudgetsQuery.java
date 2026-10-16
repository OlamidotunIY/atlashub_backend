package com.atlashub.accounting.budget.application.query.ListBudgets;

import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;

public record ListBudgetsQuery(
        Long organizationId,
        BudgetStatus status,
        String period
) {}
