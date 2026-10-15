package com.atlashub.accounting.budget.application.command.ApproveBudget;

public record ApproveBudgetCommand(
        Long budgetId,
        Long approverId
) {}
