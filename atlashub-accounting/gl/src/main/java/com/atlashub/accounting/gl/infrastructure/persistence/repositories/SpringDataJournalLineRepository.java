package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJournalLineRepository extends JpaRepository<JournalLineJpa, Long> {

    List<JournalLineJpa> findAllByJournalEntryId(Long journalEntryId);

    List<JournalLineJpa> findAllByAccountId(Long accountId);

    void deleteAllByJournalEntryId(Long journalEntryId);
}
