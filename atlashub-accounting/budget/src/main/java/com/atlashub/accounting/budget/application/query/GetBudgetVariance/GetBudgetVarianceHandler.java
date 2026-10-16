package com.atlashub.accounting.budget.application.query.GetBudgetVariance;

import com.atlashub.shared.application.port.BudgetExpenditurePort;
import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.BudgetNotFoundException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GetBudgetVarianceHandler extends Query<GetBudgetVarianceQuery, BudgetVarianceResult> {

    private final BudgetRepository budgetRepository;
    private final BudgetExpenditurePort budgetExpenditurePort;

    public GetBudgetVarianceHandler(
            BudgetRepository budgetRepository,
            BudgetExpenditurePort budgetExpenditurePort
    ) {
        this.budgetRepository = budgetRepository;
        this.budgetExpenditurePort = budgetExpenditurePort;
    }

    @Override
    public BudgetVarianceResult execute(GetBudgetVarianceQuery query) {
        Budget budget = budgetRepository.findById(query.budgetId())
                .orElseThrow(() -> new BudgetNotFoundException(query.budgetId()));

        if (query.organizationId() != null && !query.organizationId().equals(budget.getOrganizationId())) {
            throw new BudgetNotFoundException(query.budgetId());
        }

        BigDecimal budgeted = budget.getBudgetedAmount().amount();
        String currency = budget.getBudgetedAmount().currency().name();

        BigDecimal actual = budgetExpenditurePort.findActualExpenditure(
                budget.getOrganizationId(),
                budget.getAccountId(),
                budget.getPeriod()
        ).map(Money::amount).orElse(BigDecimal.ZERO);

        BigDecimal variance = budgeted.subtract(actual);
        BigDecimal variancePercentage = BigDecimal.ZERO;
        if (budgeted.compareTo(BigDecimal.ZERO) != 0) {
            variancePercentage = variance.divide(budgeted, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new BudgetVarianceResult(
                budget.getId(),
                budget.getAccountId(),
                budget.getPeriod(),
                budgeted,
                actual,
                variance,
                variancePercentage,
                currency
        );
    }
}
