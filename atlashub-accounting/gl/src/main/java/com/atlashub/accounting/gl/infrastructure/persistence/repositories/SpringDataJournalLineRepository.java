package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataJournalLineRepository extends JpaRepository<JournalLineJpa, Long> {

    List<JournalLineJpa> findAllByJournalEntryId(Long journalEntryId);

    void deleteAllByJournalEntryId(Long journalEntryId);
}
