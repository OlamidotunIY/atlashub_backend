package com.atlashub.accounting.budget.application.command.CreateBudget;

import com.atlashub.shared.domain.valueobject.Money;

public record CreateBudgetCommand(
        Long organizationId,
        Long accountId,
        String name,
        String period,
        Money budgetedAmount,
        Long initiatedBy
) {}
