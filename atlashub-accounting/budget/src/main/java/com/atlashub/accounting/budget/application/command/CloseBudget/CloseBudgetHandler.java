package com.atlashub.accounting.budget.application.command.CloseBudget;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.BudgetNotFoundException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class CloseBudgetHandler extends Command<CloseBudgetCommand, Void> {

    private final BudgetRepository budgetRepository;

    public CloseBudgetHandler(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Void execute(CloseBudgetCommand input) {
        Budget budget = budgetRepository.findById(input.budgetId())
                .orElseThrow(() -> new BudgetNotFoundException(input.budgetId()));

        budget.close();
        budgetRepository.save(budget);
        return null;
    }
}
