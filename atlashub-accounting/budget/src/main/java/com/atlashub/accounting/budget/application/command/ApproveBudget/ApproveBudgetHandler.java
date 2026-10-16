package com.atlashub.accounting.budget.application.command.ApproveBudget;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.BudgetNotFoundException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class ApproveBudgetHandler extends Command<ApproveBudgetCommand, Void> {

    private final BudgetRepository budgetRepository;

    public ApproveBudgetHandler(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Void execute(ApproveBudgetCommand input) {
        Budget budget = budgetRepository.findById(input.budgetId())
                .orElseThrow(() -> new BudgetNotFoundException(input.budgetId()));

        budget.approve(input.approverId());
        budgetRepository.save(budget);
        return null;
    }
}
