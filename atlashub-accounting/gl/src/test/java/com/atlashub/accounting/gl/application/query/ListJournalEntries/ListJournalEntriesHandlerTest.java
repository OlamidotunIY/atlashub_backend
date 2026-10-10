package com.atlashub.accounting.gl.application.query.ListJournalEntries;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListJournalEntriesHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private ListJournalEntriesHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        handler = new ListJournalEntriesHandler(journalEntryRepository);
    }

    @Test
    void execute_withoutStatusFilter_returnsAllEntriesForOrg() {
        JournalEntry entry1 = JournalEntry.create(1L, 10L, "JE-001", LocalDate.now(), null, null, EntrySource.MANUAL, null);
        JournalEntry entry2 = JournalEntry.create(2L, 10L, "JE-002", LocalDate.now(), null, null, EntrySource.MANUAL, null);

        when(journalEntryRepository.findByOrganizationId(10L)).thenReturn(List.of(entry1, entry2));

        List<JournalEntrySummaryResult> results = handler.execute(new ListJournalEntriesQuery(10L, null));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("JE-001", results.get(0).entryNumber());
        assertEquals("JE-002", results.get(1).entryNumber());
    }

    @Test
    void execute_withStatusFilter_returnsFilteredEntries() {
        JournalEntry entry = JournalEntry.create(1L, 10L, "JE-001", LocalDate.now(), null, null, EntrySource.MANUAL, null);

        when(journalEntryRepository.findByOrganizationIdAndStatus(10L, JournalEntryStatus.DRAFT))
                .thenReturn(List.of(entry));

        List<JournalEntrySummaryResult> results = handler.execute(
                new ListJournalEntriesQuery(10L, JournalEntryStatus.DRAFT)
        );

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(JournalEntryStatus.DRAFT, results.get(0).status());
    }
}
