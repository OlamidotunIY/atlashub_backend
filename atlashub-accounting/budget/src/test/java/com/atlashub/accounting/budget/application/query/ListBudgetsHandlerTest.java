package com.atlashub.accounting.budget.application.query;

import com.atlashub.accounting.budget.application.query.ListBudgets.BudgetResult;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsHandler;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsQuery;
import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListBudgetsHandlerTest {

    private BudgetRepository budgetRepository;
    private ListBudgetsHandler handler;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        handler = new ListBudgetsHandler(budgetRepository);
    }

    @Test
    @DisplayName("Should list budgets for organization without filters")
    void shouldListBudgetsWithoutFilters() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q2",
                "2026-Q2",
                Money.of(new BigDecimal("1500000.00"), CurrencyCode.NGN),
                20L
        );

        when(budgetRepository.findByOrganizationId(10L)).thenReturn(List.of(budget));

        List<BudgetResult> results = handler.execute(new ListBudgetsQuery(10L, null, null));

        assertEquals(1, results.size());
        assertEquals("Marketing Q2", results.getFirst().name());
    }

    @Test
    @DisplayName("Should list budgets with status and period filters")
    void shouldListBudgetsWithFilters() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q2",
                "2026-Q2",
                Money.of(new BigDecimal("1500000.00"), CurrencyCode.NGN),
                20L
        );
        budget.approve(30L);

        when(budgetRepository.findByOrganizationIdAndStatusAndPeriod(10L, BudgetStatus.APPROVED, "2026-Q2"))
                .thenReturn(List.of(budget));

        List<BudgetResult> results = handler.execute(new ListBudgetsQuery(10L, BudgetStatus.APPROVED, "2026-Q2"));

        assertEquals(1, results.size());
        assertEquals(BudgetStatus.APPROVED, results.getFirst().status());
    }
}
