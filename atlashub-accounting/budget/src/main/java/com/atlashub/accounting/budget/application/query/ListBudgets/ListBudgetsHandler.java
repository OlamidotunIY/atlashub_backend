package com.atlashub.accounting.budget.application.query.ListBudgets;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListBudgetsHandler extends Query<ListBudgetsQuery, List<BudgetResult>> {

    private final BudgetRepository budgetRepository;

    public ListBudgetsHandler(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public List<BudgetResult> execute(ListBudgetsQuery query) {
        List<Budget> budgets;

        if (query.status() != null && query.period() != null && !query.period().isBlank()) {
            budgets = budgetRepository.findByOrganizationIdAndStatusAndPeriod(
                    query.organizationId(),
                    query.status(),
                    query.period()
            );
        } else if (query.status() != null) {
            budgets = budgetRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else if (query.period() != null && !query.period().isBlank()) {
            budgets = budgetRepository.findByOrganizationIdAndPeriod(query.organizationId(), query.period());
        } else {
            budgets = budgetRepository.findByOrganizationId(query.organizationId());
        }

        return budgets.stream()
                .map(this::toResult)
                .toList();
    }

    private BudgetResult toResult(Budget budget) {
        return new BudgetResult(
                budget.getId(),
                budget.getOrganizationId(),
                budget.getAccountId(),
                budget.getName(),
                budget.getPeriod(),
                budget.getBudgetedAmount().amount(),
                budget.getBudgetedAmount().currency().name(),
                budget.getStatus(),
                budget.getInitiatedBy(),
                budget.getApprovedBy(),
                budget.getApprovedAt()
        );
    }
}
