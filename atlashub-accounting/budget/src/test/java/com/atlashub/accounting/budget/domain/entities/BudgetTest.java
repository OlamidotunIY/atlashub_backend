package com.atlashub.accounting.budget.domain.entities;

import com.atlashub.accounting.budget.domain.events.BudgetApprovedEvent;
import com.atlashub.accounting.budget.domain.exceptions.BudgetAlreadyApprovedException;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetOperationException;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetStateException;
import com.atlashub.accounting.budget.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BudgetTest {

    @Test
    @DisplayName("Should create Budget with valid attributes and initial DRAFT state")
    void shouldCreateBudgetSuccessfully() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("2000000.00"), CurrencyCode.NGN),
                50L
        );

        assertNotNull(budget);
        assertEquals(1L, budget.getId());
        assertEquals(10L, budget.getOrganizationId());
        assertEquals(1001L, budget.getAccountId());
        assertEquals("Marketing Q3", budget.getName());
        assertEquals("2026-Q3", budget.getPeriod());
        assertEquals(BudgetStatus.DRAFT, budget.getStatus());
        assertEquals(50L, budget.getInitiatedBy());
        assertNull(budget.getApprovedBy());
        assertNull(budget.getApprovedAt());
        assertThat(budget.getBudgetedAmount().amount()).isEqualByComparingTo(new BigDecimal("2000000.00"));
    }

    @Test
    @DisplayName("Should fail creation when invariants are violated")
    void shouldFailCreationOnInvalidInvariants() {
        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                null,
                10L,
                1001L,
                "Marketing",
                "2026-Q3",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                null,
                1001L,
                "Marketing",
                "2026-Q3",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                10L,
                null,
                "Marketing",
                "2026-Q3",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                10L,
                1001L,
                "",
                "2026-Q3",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                10L,
                1001L,
                "Marketing",
                "",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                10L,
                1001L,
                "Marketing",
                "2026-Q3",
                Money.of(BigDecimal.ZERO, CurrencyCode.NGN),
                50L
        ));

        assertThrows(InvalidBudgetOperationException.class, () -> Budget.create(
                1L,
                10L,
                1001L,
                "Marketing",
                "2026-Q3",
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                null
        ));
    }

    @Test
    @DisplayName("Should enforce maker-checker: approver cannot be the initiator")
    void shouldThrowSelfApprovalNotAllowedException() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("2000000.00"), CurrencyCode.NGN),
                50L
        );

        assertThrows(SelfApprovalNotAllowedException.class, () -> budget.approve(50L));
    }

    @Test
    @DisplayName("Should approve budget and register BudgetApprovedEvent")
    void shouldApproveBudgetSuccessfully() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("2000000.00"), CurrencyCode.NGN),
                50L
        );

        budget.approve(99L);

        assertEquals(BudgetStatus.APPROVED, budget.getStatus());
        assertEquals(99L, budget.getApprovedBy());
        assertNotNull(budget.getApprovedAt());
        assertEquals(1, budget.peekDomainEvents().size());
        assertThat(budget.peekDomainEvents().getFirst()).isInstanceOf(BudgetApprovedEvent.class);

        // Attempting to approve again should throw BudgetAlreadyApprovedException
        assertThrows(BudgetAlreadyApprovedException.class, () -> budget.approve(100L));
    }

    @Test
    @DisplayName("Should close approved budget successfully")
    void shouldCloseBudgetSuccessfully() {
        Budget budget = Budget.create(
                1L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                Money.of(new BigDecimal("2000000.00"), CurrencyCode.NGN),
                50L
        );

        // Cannot close in DRAFT status
        assertThrows(InvalidBudgetStateException.class, budget::close);

        budget.approve(99L);
        budget.close();

        assertEquals(BudgetStatus.CLOSED, budget.getStatus());
        assertThrows(InvalidBudgetStateException.class, () -> budget.approve(100L));
    }
}
