package com.atlashub.accounting.gl.application.command.VoidJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VoidJournalEntryHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private VoidJournalEntryHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        handler = new VoidJournalEntryHandler(journalEntryRepository);
    }

    @Test
    void execute_voidsPostedJournalEntrySuccessfully() {
        JournalEntry entry = JournalEntry.create(
                100L,
                10L,
                "JE-001",
                LocalDate.now(),
                "REF",
                "Description",
                EntrySource.MANUAL,
                1001L
        );
        entry.addLine(1L, 10L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.DEBIT);
        entry.addLine(2L, 20L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.CREDIT);
        entry.post();

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoidJournalEntryResult result = handler.execute(
                new VoidJournalEntryCommand(100L, 10L, 2002L, "Mistake entry")
        );

        assertEquals(JournalEntryStatus.VOIDED, result.status());
        assertEquals("JE-001", result.entryNumber());
        verify(journalEntryRepository).save(entry);
    }

    @Test
    void execute_draftEntry_throwsInvalidEntryStateException() {
        JournalEntry entry = JournalEntry.create(
                100L,
                10L,
                "JE-001",
                LocalDate.now(),
                "REF",
                "Description",
                EntrySource.MANUAL,
                1001L
        );

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));

        assertThrows(InvalidEntryStateException.class,
                () -> handler.execute(new VoidJournalEntryCommand(100L, 10L, 2002L, "Reason")));
    }

    @Test
    void execute_entryNotFound_throwsJournalEntryNotFoundException() {
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(JournalEntryNotFoundException.class,
                () -> handler.execute(new VoidJournalEntryCommand(999L, 10L, 2002L, "Reason")));
    }
}
