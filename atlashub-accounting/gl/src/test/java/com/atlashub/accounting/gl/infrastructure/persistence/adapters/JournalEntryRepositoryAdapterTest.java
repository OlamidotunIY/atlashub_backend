package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalEntryJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.JournalEntryMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.JournalLineMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalEntryRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalLineRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JournalEntryRepositoryAdapterTest {

    private SpringDataJournalEntryRepository entryRepo;
    private SpringDataJournalLineRepository lineRepo;
    private JournalEntryMapper entryMapper;
    private JournalLineMapper lineMapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private JournalEntryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        entryRepo = mock(SpringDataJournalEntryRepository.class);
        lineRepo = mock(SpringDataJournalLineRepository.class);
        entryMapper = mock(JournalEntryMapper.class);
        lineMapper = mock(JournalLineMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new JournalEntryRepositoryAdapter(
                entryRepo, lineRepo, entryMapper, lineMapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void nextIdentity_delegatesToSequenceGenerator() {
        when(sequenceGenerator.nextIdentity("accounting_journal_entry_seq")).thenReturn(501L);

        Long id = adapter.nextIdentity();

        assertEquals(501L, id);
        verify(sequenceGenerator).nextIdentity("accounting_journal_entry_seq");
    }

    @Test
    void findById_loadsLinesAndMapsToDomain() {
        JournalEntryJpa jpa = new JournalEntryJpa(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc",
                JournalEntryStatus.DRAFT, EntrySource.MANUAL, 1001L, null, null, ZonedDateTime.now(), 0L);
        JournalEntry domain = JournalEntry.create(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc", EntrySource.MANUAL, 1001L);

        when(entryRepo.findById(100L)).thenReturn(Optional.of(jpa));
        when(lineRepo.findAllByJournalEntryId(100L)).thenReturn(List.of());
        when(entryMapper.toDomain(jpa, List.of())).thenReturn(domain);

        Optional<JournalEntry> result = adapter.findById(100L);

        assertTrue(result.isPresent());
        assertEquals("JE-001", result.get().getEntryNumber());
        verify(lineRepo).findAllByJournalEntryId(100L);
    }

    @Test
    void findByEntryNumber_mapsToFullDomain() {
        JournalEntryJpa jpa = new JournalEntryJpa(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc",
                JournalEntryStatus.DRAFT, EntrySource.MANUAL, 1001L, null, null, ZonedDateTime.now(), 0L);
        JournalEntry domain = JournalEntry.create(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc", EntrySource.MANUAL, 1001L);

        when(entryRepo.findByEntryNumber("JE-001")).thenReturn(Optional.of(jpa));
        when(lineRepo.findAllByJournalEntryId(100L)).thenReturn(List.of());
        when(entryMapper.toDomain(jpa, List.of())).thenReturn(domain);

        Optional<JournalEntry> result = adapter.findByEntryNumber("JE-001");

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getId());
    }

    @Test
    void save_persistsEntryAndLines() {
        JournalEntry domain = JournalEntry.create(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc", EntrySource.MANUAL, 1001L);
        JournalEntryJpa jpa = new JournalEntryJpa(
                100L, 10L, "JE-001", LocalDate.now(), "REF", "Desc",
                JournalEntryStatus.DRAFT, EntrySource.MANUAL, 1001L, null, null, ZonedDateTime.now(), 0L);

        when(entryRepo.findById(100L)).thenReturn(Optional.empty());
        when(entryMapper.toPersistence(domain)).thenReturn(jpa);
        when(entryRepo.save(jpa)).thenReturn(jpa);
        when(entryMapper.toDomain(any(JournalEntryJpa.class), anyList())).thenReturn(domain);

        JournalEntry saved = adapter.save(domain);

        assertNotNull(saved);
        verify(lineRepo).deleteAllByJournalEntryId(100L);
        verify(lineRepo).saveAll(anyList());
        verify(entryRepo).save(jpa);
    }

    @Test
    void deleteById_deletesLinesAndEntry() {
        adapter.deleteById(100L);

        verify(lineRepo).deleteAllByJournalEntryId(100L);
        verify(entryRepo).deleteById(100L);
    }
}
