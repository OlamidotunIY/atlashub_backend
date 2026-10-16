package com.atlashub.accounting.budget.application.command;

import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetCommand;
import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetHandler;
import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetOperationException;
import com.atlashub.accounting.budget.domain.repositories.BudgetRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBudgetHandlerTest {

    private BudgetRepository budgetRepository;
    private CreateBudgetHandler handler;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        handler = new CreateBudgetHandler(budgetRepository);
    }

    @Test
    @DisplayName("Should create and save budget successfully")
    void shouldCreateBudgetSuccessfully() {
        CreateBudgetCommand command = new CreateBudgetCommand(
                10L,
                1001L,
                "IT Infrastructure Q4",
                "2026-Q4",
                Money.of(new BigDecimal("5000000.00"), CurrencyCode.NGN),
                20L
        );

        when(budgetRepository.findByOrganizationIdAndAccountIdAndPeriod(10L, 1001L, "2026-Q4"))
                .thenReturn(Optional.empty());
        when(budgetRepository.nextIdentity()).thenReturn(201L);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        Long budgetId = handler.execute(command);

        assertEquals(201L, budgetId);
        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());

        Budget saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(201L, saved.getId());
        assertEquals("IT Infrastructure Q4", saved.getName());
    }

    @Test
    @DisplayName("Should throw exception when budget already exists for account and period")
    void shouldThrowWhenBudgetAlreadyExists() {
        CreateBudgetCommand command = new CreateBudgetCommand(
                10L,
                1001L,
                "IT Infrastructure Q4",
                "2026-Q4",
                Money.of(new BigDecimal("5000000.00"), CurrencyCode.NGN),
                20L
        );

        Budget existing = mock(Budget.class);
        when(budgetRepository.findByOrganizationIdAndAccountIdAndPeriod(10L, 1001L, "2026-Q4"))
                .thenReturn(Optional.of(existing));

        assertThrows(InvalidBudgetOperationException.class, () -> handler.execute(command));
    }
}
