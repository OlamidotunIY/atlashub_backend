package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.events.JournalEntryPendingApprovalEvent;
import com.atlashub.accounting.gl.domain.events.JournalEntryPostedEvent;
import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.exceptions.JournalUnbalancedException;
import com.atlashub.accounting.gl.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JournalEntryTest {

    @Test
    @DisplayName("Should create draft journal entry")
    void shouldCreateDraftJournalEntry() {
        JournalEntry entry = JournalEntry.create(
                10L,
                1L,
                "JE-2026-09-001",
                LocalDate.of(2026, 9, 30),
                "REF-001",
                "Monthly accrual",
                EntrySource.MANUAL,
                100L
        );

        assertEquals(10L, entry.getId());
        assertEquals(JournalEntryStatus.DRAFT, entry.getStatus());
        assertEquals("JE-2026-09-001", entry.getEntryNumber());
        assertEquals(100L, entry.getInitiatedBy());
        assertTrue(entry.getLines().isEmpty());
        assertNotNull(entry.getCreatedAt());
    }

    @Test
    @DisplayName("Should add lines, calculate debits and credits, and post balanced entry")
    void shouldPostBalancedEntry() {
        JournalEntry entry = JournalEntry.create(
                10L,
                1L,
                "JE-2026-09-002",
                LocalDate.of(2026, 9, 30),
                "REF-002",
                "Rent payment",
                EntrySource.AUTOMATIC,
                null
        );

        Money rentAmount = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 501L, rentAmount, EntryType.DEBIT);
        entry.addLine(2L, 101L, rentAmount, EntryType.CREDIT);

        assertEquals(2, entry.getLines().size());
        assertTrue(entry.isBalanced());
        assertEquals(rentAmount, entry.calculateTotalDebits());
        assertEquals(rentAmount, entry.calculateTotalCredits());

        entry.post();

        assertEquals(JournalEntryStatus.POSTED, entry.getStatus());
        assertEquals(1, entry.peekDomainEvents().size());
        assertTrue(entry.peekDomainEvents().get(0) instanceof JournalEntryPostedEvent);
    }

    @Test
    @DisplayName("Should throw JournalUnbalancedException when posting unbalanced entry")
    void shouldThrowWhenPostingUnbalancedEntry() {
        JournalEntry entry = JournalEntry.create(
                11L,
                1L,
                "JE-2026-09-003",
                LocalDate.of(2026, 9, 30),
                "REF-003",
                "Unbalanced entry",
                EntrySource.AUTOMATIC,
                null
        );

        entry.addLine(1L, 501L, Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN), EntryType.DEBIT);
        entry.addLine(2L, 101L, Money.of(new BigDecimal("90000.00"), CurrencyCode.NGN), EntryType.CREDIT);

        assertFalse(entry.isBalanced());
        assertThrows(JournalUnbalancedException.class, entry::post);
    }

    @Test
    @DisplayName("Should submit draft entry for approval")
    void shouldSubmitForApproval() {
        JournalEntry entry = JournalEntry.create(
                12L,
                1L,
                "JE-2026-09-004",
                LocalDate.of(2026, 9, 30),
                "REF-004",
                "Large capital expenditure",
                EntrySource.MANUAL,
                100L
        );

        Money capex = Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 150L, capex, EntryType.DEBIT);
        entry.addLine(2L, 101L, capex, EntryType.CREDIT);

        entry.submitForApproval(100L);

        assertEquals(JournalEntryStatus.PENDING_APPROVAL, entry.getStatus());
        assertEquals(1, entry.peekDomainEvents().size());
        assertTrue(entry.peekDomainEvents().get(0) instanceof JournalEntryPendingApprovalEvent);
    }

    @Test
    @DisplayName("Should reject self-approval with SelfApprovalNotAllowedException")
    void shouldRejectSelfApproval() {
        JournalEntry entry = JournalEntry.create(
                13L,
                1L,
                "JE-2026-09-005",
                LocalDate.of(2026, 9, 30),
                "REF-005",
                "Manual adjustment",
                EntrySource.MANUAL,
                100L
        );

        Money amount = Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 501L, amount, EntryType.DEBIT);
        entry.addLine(2L, 101L, amount, EntryType.CREDIT);
        entry.submitForApproval(100L);

        assertThrows(SelfApprovalNotAllowedException.class, () -> entry.approve(100L));
    }

    @Test
    @DisplayName("Should approve entry with different checker approver")
    void shouldApproveWithDifferentChecker() {
        JournalEntry entry = JournalEntry.create(
                14L,
                1L,
                "JE-2026-09-006",
                LocalDate.of(2026, 9, 30),
                "REF-006",
                "Manual adjustment",
                EntrySource.MANUAL,
                100L
        );

        Money amount = Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 501L, amount, EntryType.DEBIT);
        entry.addLine(2L, 101L, amount, EntryType.CREDIT);
        entry.submitForApproval(100L);

        entry.approve(200L);

        assertEquals(JournalEntryStatus.POSTED, entry.getStatus());
        assertEquals(200L, entry.getApprovedBy());
        assertNotNull(entry.getApprovedAt());
    }

    @Test
    @DisplayName("Should void posted entry")
    void shouldVoidPostedEntry() {
        JournalEntry entry = JournalEntry.create(
                15L,
                1L,
                "JE-2026-09-007",
                LocalDate.of(2026, 9, 30),
                "REF-007",
                "Consulting",
                EntrySource.AUTOMATIC,
                null
        );

        Money amount = Money.of(new BigDecimal("25000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 502L, amount, EntryType.DEBIT);
        entry.addLine(2L, 101L, amount, EntryType.CREDIT);
        entry.post();

        entry.voidEntry(100L, "Cancelled contract");
        assertEquals(JournalEntryStatus.VOIDED, entry.getStatus());
    }

    @Test
    @DisplayName("Should throw InvalidEntryStateException when adding line to non-draft entry")
    void shouldThrowWhenAddingLineToNonDraftEntry() {
        JournalEntry entry = JournalEntry.create(
                16L,
                1L,
                "JE-2026-09-008",
                LocalDate.of(2026, 9, 30),
                "REF-008",
                "Test",
                EntrySource.AUTOMATIC,
                null
        );

        Money amount = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        entry.addLine(1L, 501L, amount, EntryType.DEBIT);
        entry.addLine(2L, 101L, amount, EntryType.CREDIT);
        entry.post();

        assertThrows(InvalidEntryStateException.class, () ->
                entry.addLine(3L, 502L, amount, EntryType.DEBIT));
    }
}
