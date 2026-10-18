package com.atlashub.accounting.budget.application.query;

import com.atlashub.shared.application.port.BudgetExpenditurePort;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.BudgetVarianceResult;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceHandler;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceQuery;
import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.BudgetNotFoundException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetBudgetVarianceHandlerTest {

    private BudgetRepository budgetRepository;
    private BudgetExpenditurePort budgetExpenditurePort;
    private GetBudgetVarianceHandler handler;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        budgetExpenditurePort = mock(BudgetExpenditurePort.class);
        handler = new GetBudgetVarianceHandler(budgetRepository, budgetExpenditurePort);
    }

    @Test
    @DisplayName("Should compute budget variance correctly when actuals exist")
    void shouldComputeBudgetVariance() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN),
                20L
        );

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetExpenditurePort.findActualExpenditure(10L, 1001L, "2026-Q3"))
                .thenReturn(Optional.of(Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN)));

        BudgetVarianceResult result = handler.execute(new GetBudgetVarianceQuery(1L, 10L));

        assertNotNull(result);
        assertEquals(1L, result.budgetId());
        assertEquals(1001L, result.accountId());
        assertEquals("2026-Q3", result.period());
        assertThat(result.budgetedAmount()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(result.actualAmount()).isEqualByComparingTo(new BigDecimal("600000.00"));
        assertThat(result.variance()).isEqualByComparingTo(new BigDecimal("400000.00"));
        assertThat(result.variancePercentage()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("Should default actuals to zero when none recorded")
    void shouldDefaultActualsToZero() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN),
                20L
        );

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetExpenditurePort.findActualExpenditure(10L, 1001L, "2026-Q3"))
                .thenReturn(Optional.empty());

        BudgetVarianceResult result = handler.execute(new GetBudgetVarianceQuery(1L, 10L));

        assertThat(result.actualAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.variance()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(result.variancePercentage()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw BudgetNotFoundException on org mismatch or not found")
    void shouldThrowWhenNotFoundOrOrgMismatch() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN),
                20L
        );

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        // Org mismatch (requesting org 99 instead of 10)
        assertThrows(BudgetNotFoundException.class, () -> handler.execute(new GetBudgetVarianceQuery(1L, 99L)));

        // Not found
        assertThrows(BudgetNotFoundException.class, () -> handler.execute(new GetBudgetVarianceQuery(999L, 10L)));
    }
}
