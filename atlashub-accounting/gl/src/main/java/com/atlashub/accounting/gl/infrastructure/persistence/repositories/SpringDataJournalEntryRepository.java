package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalEntryJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataJournalEntryRepository extends JpaRepository<JournalEntryJpa, Long> {

    Optional<JournalEntryJpa> findByEntryNumber(String entryNumber);

    List<JournalEntryJpa> findByOrganizationId(Long organizationId);

    List<JournalEntryJpa> findByOrganizationIdAndStatus(Long organizationId, JournalEntryStatus status);

    List<JournalEntryJpa> findByOrganizationIdAndDateBetween(Long organizationId, LocalDate from, LocalDate to);
}
