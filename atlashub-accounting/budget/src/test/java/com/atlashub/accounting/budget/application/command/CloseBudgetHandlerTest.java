package com.atlashub.accounting.budget.application.command;

import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetCommand;
import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetHandler;
import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.BudgetNotFoundException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloseBudgetHandlerTest {

    private BudgetRepository budgetRepository;
    private CloseBudgetHandler handler;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        handler = new CloseBudgetHandler(budgetRepository);
    }

    @Test
    @DisplayName("Should close approved budget successfully")
    void shouldCloseBudgetSuccessfully() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Legal & Compliance",
                "2026-Q1",
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN),
                20L
        );
        budget.approve(30L);

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        handler.execute(new CloseBudgetCommand(1L));

        assertEquals(BudgetStatus.CLOSED, budget.getStatus());
        verify(budgetRepository).save(budget);
    }

    @Test
    @DisplayName("Should throw BudgetNotFoundException when budget is missing")
    void shouldThrowWhenBudgetNotFound() {
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BudgetNotFoundException.class, () -> handler.execute(new CloseBudgetCommand(999L)));
    }
}
