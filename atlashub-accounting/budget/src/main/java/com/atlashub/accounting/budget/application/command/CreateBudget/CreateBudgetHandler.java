package com.atlashub.accounting.budget.application.command.CreateBudget;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetOperationException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CreateBudgetHandler extends Command<CreateBudgetCommand, Long> {

    private final BudgetRepository budgetRepository;

    public CreateBudgetHandler(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Long execute(CreateBudgetCommand input) {
        Optional<Budget> existing = budgetRepository.findByOrganizationIdAndAccountIdAndPeriod(
                input.organizationId(),
                input.accountId(),
                input.period()
        );

        if (existing.isPresent()) {
            throw new InvalidBudgetOperationException(
                    "A budget already exists for organization " + input.organizationId()
                            + ", account " + input.accountId()
                            + " and period " + input.period()
            );
        }

        Long budgetId = budgetRepository.nextIdentity();
        Budget budget = Budget.create(
                budgetId,
                input.organizationId(),
                input.accountId(),
                input.name(),
                input.period(),
                input.budgetedAmount(),
                input.initiatedBy()
        );

        budgetRepository.save(budget);
        return budgetId;
    }
}
