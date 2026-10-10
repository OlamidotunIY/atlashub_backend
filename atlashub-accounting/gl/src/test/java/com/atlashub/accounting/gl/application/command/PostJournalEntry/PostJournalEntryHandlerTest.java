package com.atlashub.accounting.gl.application.command.PostJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.exceptions.JournalUnbalancedException;
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

class PostJournalEntryHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private PostJournalEntryHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        handler = new PostJournalEntryHandler(journalEntryRepository);
    }

    @Test
    void execute_postsBalancedJournalEntrySuccessfully() {
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

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostJournalEntryResult result = handler.execute(new PostJournalEntryCommand(100L, 10L));

        assertEquals(JournalEntryStatus.POSTED, result.status());
        assertEquals(Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), result.totalDebits());
        assertEquals(Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), result.totalCredits());
        verify(journalEntryRepository).save(entry);
    }

    @Test
    void execute_unbalancedEntry_throwsJournalUnbalancedException() {
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

        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));

        assertThrows(JournalUnbalancedException.class,
                () -> handler.execute(new PostJournalEntryCommand(100L, 10L)));
    }

    @Test
    void execute_entryNotFound_throwsJournalEntryNotFoundException() {
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(JournalEntryNotFoundException.class,
                () -> handler.execute(new PostJournalEntryCommand(999L, 10L)));
    }
}
