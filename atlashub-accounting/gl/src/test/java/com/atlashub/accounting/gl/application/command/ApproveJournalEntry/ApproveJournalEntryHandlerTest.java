package com.atlashub.accounting.gl.application.command.ApproveJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApproveJournalEntryHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private ApproveJournalEntryHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        handler = new ApproveJournalEntryHandler(journalEntryRepository);
    }

    @Test
    void execute_approvesPendingJournalEntrySuccessfully() {
        JournalEntry entry = JournalEntry.create(
                100L,
                10L,
                "JE-001",
                LocalDate.now(),
                "REF",
                "Description",
                EntrySource.MANUAL,
                null
        );
        entry.addLine(1L, 10L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.DEBIT);
        entry.addLine(2L, 20L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.CREDIT);
        entry.submitForApproval(1001L);

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApproveJournalEntryResult result = handler.execute(
                new ApproveJournalEntryCommand(100L, 10L, 2002L)
        );

        assertEquals(JournalEntryStatus.POSTED, result.status());
        assertEquals(2002L, result.approvedBy());
        assertNotNull(result.approvedAt());
        verify(journalEntryRepository).save(entry);
    }

    @Test
    void execute_sameMakerAndChecker_throwsSelfApprovalNotAllowedException() {
        JournalEntry entry = JournalEntry.create(
                100L,
                10L,
                "JE-001",
                LocalDate.now(),
                "REF",
                "Description",
                EntrySource.MANUAL,
                null
        );
        entry.addLine(1L, 10L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.DEBIT);
        entry.addLine(2L, 20L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.CREDIT);
        entry.submitForApproval(1001L);

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));

        assertThrows(SelfApprovalNotAllowedException.class,
                () -> handler.execute(new ApproveJournalEntryCommand(100L, 10L, 1001L)));
    }

    @Test
    void execute_entryNotFound_throwsJournalEntryNotFoundException() {
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(JournalEntryNotFoundException.class,
                () -> handler.execute(new ApproveJournalEntryCommand(999L, 10L, 2002L)));
    }
}
