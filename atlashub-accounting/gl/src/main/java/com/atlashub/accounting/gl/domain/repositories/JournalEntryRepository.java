package com.atlashub.accounting.gl.domain.repositories;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends Repository<JournalEntry> {

    Optional<JournalEntry> findByEntryNumber(String entryNumber);

    List<JournalEntry> findByOrganizationId(Long organizationId);

    List<JournalEntry> findByOrganizationIdAndStatus(Long organizationId, JournalEntryStatus status);

    List<JournalEntry> findByOrganizationIdAndDateBetween(Long organizationId, LocalDate from, LocalDate to);
}
