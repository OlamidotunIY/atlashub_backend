package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.shared.domain.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerTransactionRepository extends Repository<LedgerTransaction> {
    Optional<LedgerTransaction> findByReference(String reference);
    List<LedgerTransaction> findByOrganizationId(Long organizationId, Pageable pageable);
    
    Page<LedgerTransaction> findHistory(Long organizationId, Long accountId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    List<LedgerTransaction> findByAccountIdAndPostedAtAfter(Long accountId, java.time.ZonedDateTime postedAt);
}
