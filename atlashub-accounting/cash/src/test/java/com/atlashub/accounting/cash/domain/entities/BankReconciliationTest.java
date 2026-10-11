package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.events.BankReconciliationCompletedEvent;
import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.exceptions.ReconciliationNotBalancedException;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankReconciliationTest {

    @Test
    @DisplayName("Should create draft bank reconciliation and calculate difference")
    void create_valid_draftReconciliation() {
        Money statementBalance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        Money bookBalance = Money.of(new BigDecimal("800.00"), CurrencyCode.NGN);

        BankStatementLine line1 = BankStatementLine.create(
                1L, 100L, LocalDate.now(), "Deposit", Money.of(new BigDecimal("200.00"), CurrencyCode.NGN), StatementEntryType.CREDIT);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), statementBalance, bookBalance,
                List.of(line1), List.of(501L));

        assertEquals(100L, reconciliation.getId());
        assertEquals(10L, reconciliation.getOrganizationId());
        assertEquals("1010-BANK", reconciliation.getBankAccountCode());
        assertEquals(ReconciliationStatus.DRAFT, reconciliation.getStatus());
        assertEquals(Money.of(new BigDecimal("200.00"), CurrencyCode.NGN), reconciliation.getDifference());
        assertEquals(1, reconciliation.getUnmatchedBankItems().size());
        assertEquals(1, reconciliation.getUnmatchedBookItems().size());
        assertTrue(reconciliation.getMatchedItems().isEmpty());
    }

    @Test
    @DisplayName("Should throw when statement and book currencies differ")
    void create_currencyMismatch_throwsException() {
        Money statementBalance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        Money bookBalance = Money.of(new BigDecimal("1000.00"), CurrencyCode.USD);

        assertThrows(InvalidReconciliationStateException.class, () ->
                BankReconciliation.create(
                        100L, 10L, "1010-BANK", LocalDate.now(), statementBalance, bookBalance,
                        List.of(), List.of()));
    }

    @Test
    @DisplayName("Should match bank item and journal line item successfully")
    void matchItem_validItems_movesToMatched() {
        Money balance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        BankStatementLine line1 = BankStatementLine.create(
                1L, 100L, LocalDate.now(), "Deposit", Money.of(new BigDecimal("200.00"), CurrencyCode.NGN), StatementEntryType.CREDIT);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), balance, balance,
                List.of(line1), List.of(501L));

        reconciliation.matchItem(9001L, 1L, 501L);

        assertEquals(1, reconciliation.getMatchedItems().size());
        assertEquals(9001L, reconciliation.getMatchedItems().get(0).getId());
        assertEquals(1L, reconciliation.getMatchedItems().get(0).getBankStatementLineId());
        assertEquals(501L, reconciliation.getMatchedItems().get(0).getJournalLineId());
        assertTrue(reconciliation.getUnmatchedBankItems().isEmpty());
        assertTrue(reconciliation.getUnmatchedBookItems().isEmpty());
    }

    @Test
    @DisplayName("Should throw when matching bank line not in unmatched list")
    void matchItem_unknownBankLine_throwsException() {
        Money balance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), balance, balance,
                List.of(), List.of(501L));

        assertThrows(InvalidReconciliationStateException.class, () ->
                reconciliation.matchItem(9001L, 999L, 501L));
    }

    @Test
    @DisplayName("Should throw when matching journal line not in unmatched list")
    void matchItem_unknownJournalLine_throwsException() {
        Money balance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        BankStatementLine line1 = BankStatementLine.create(
                1L, 100L, LocalDate.now(), "Deposit", Money.of(new BigDecimal("200.00"), CurrencyCode.NGN), StatementEntryType.CREDIT);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), balance, balance,
                List.of(line1), List.of());

        assertThrows(InvalidReconciliationStateException.class, () ->
                reconciliation.matchItem(9001L, 1L, 999L));
    }

    @Test
    @DisplayName("Should throw ReconciliationNotBalancedException when finalizing with difference and not force")
    void finalize_unbalancedNotForced_throwsException() {
        Money statementBalance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        Money bookBalance = Money.of(new BigDecimal("800.00"), CurrencyCode.NGN);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), statementBalance, bookBalance,
                List.of(), List.of());

        assertThrows(ReconciliationNotBalancedException.class, () ->
                reconciliation.finalizeReconciliation(false));
    }

    @Test
    @DisplayName("Should finalize balanced reconciliation and register event")
    void finalize_balanced_success() {
        Money balance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), balance, balance,
                List.of(), List.of());

        reconciliation.finalizeReconciliation(false);

        assertEquals(ReconciliationStatus.RECONCILED, reconciliation.getStatus());
        assertNotNull(reconciliation.getReconciledAt());

        List<DomainEvent<?>> events = reconciliation.pullDomainEvents();
        assertFalse(events.isEmpty());
        assertTrue(events.get(0) instanceof BankReconciliationCompletedEvent);
        BankReconciliationCompletedEvent event = (BankReconciliationCompletedEvent) events.get(0);
        assertEquals(100L, event.payload().reconciliationId());
        assertEquals("1010-BANK", event.payload().bankAccountCode());
    }

    @Test
    @DisplayName("Should force finalize unbalanced reconciliation when forceFinalize is true")
    void finalize_unbalancedForceFinalize_success() {
        Money statementBalance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        Money bookBalance = Money.of(new BigDecimal("800.00"), CurrencyCode.NGN);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), statementBalance, bookBalance,
                List.of(), List.of());

        reconciliation.finalizeReconciliation(true);

        assertEquals(ReconciliationStatus.RECONCILED, reconciliation.getStatus());
        assertFalse(reconciliation.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should throw when finalizing already reconciled reconciliation")
    void finalize_alreadyReconciled_throwsException() {
        Money balance = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);

        BankReconciliation reconciliation = BankReconciliation.create(
                100L, 10L, "1010-BANK", LocalDate.now(), balance, balance,
                List.of(), List.of());

        reconciliation.finalizeReconciliation(false);

        assertThrows(InvalidReconciliationStateException.class, () ->
                reconciliation.finalizeReconciliation(false));
    }
}
