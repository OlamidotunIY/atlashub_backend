package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerEntryJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryJpa, Long> {
    List<LedgerEntryJpa> findByTransactionId(Long transactionId);
    List<LedgerEntryJpa> findByTransactionIdIn(List<Long> transactionIds);
}
