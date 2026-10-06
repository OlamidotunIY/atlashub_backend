package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerTransactionRepository extends Repository<LedgerTransaction> {
    Long nextEntryIdentity();
    Optional<LedgerTransaction> findByReferenceAndEnvironment(String reference, ApiEnvironment environment);
    List<LedgerTransaction> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment, Pageable pageable);
    
    Page<LedgerTransaction> findHistory(Long organizationId, ApiEnvironment environment, Long accountId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
    List<LedgerTransaction> findByAccountIdAndEnvironmentAndPostedAtAfter(Long accountId, ApiEnvironment environment, java.time.ZonedDateTime postedAt);
}
