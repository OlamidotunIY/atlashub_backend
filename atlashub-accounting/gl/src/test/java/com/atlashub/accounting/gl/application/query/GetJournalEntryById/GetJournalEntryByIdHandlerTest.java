package com.atlashub.accounting.gl.application.query.GetJournalEntryById;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetJournalEntryByIdHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private GetJournalEntryByIdHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        handler = new GetJournalEntryByIdHandler(journalEntryRepository);
    }

    @Test
    void execute_returnsJournalEntryWithLinesSuccessfully() {
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

        JournalEntryResult result = handler.execute(new GetJournalEntryByIdQuery(100L, 10L));

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals("JE-001", result.entryNumber());
        assertEquals(JournalEntryStatus.DRAFT, result.status());
        assertEquals(2, result.lines().size());
        assertEquals(Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), result.totalDebits());
        assertEquals(Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), result.totalCredits());
    }

    @Test
    void execute_entryNotFound_throwsJournalEntryNotFoundException() {
        when(journalEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(JournalEntryNotFoundException.class,
                () -> handler.execute(new GetJournalEntryByIdQuery(999L, 10L)));
    }

    @Test
    void execute_wrongOrganization_throwsJournalEntryNotFoundException() {
        JournalEntry entry = JournalEntry.create(
                100L,
                20L,
                "JE-001",
                LocalDate.now(),
                "REF",
                "Description",
                EntrySource.MANUAL,
                1001L
        );
        when(journalEntryRepository.findById(100L)).thenReturn(Optional.of(entry));

        assertThrows(JournalEntryNotFoundException.class,
                () -> handler.execute(new GetJournalEntryByIdQuery(100L, 10L)));
    }
}
