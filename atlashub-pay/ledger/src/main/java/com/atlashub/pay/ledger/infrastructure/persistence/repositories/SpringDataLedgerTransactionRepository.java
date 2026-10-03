package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerTransactionJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface SpringDataLedgerTransactionRepository extends JpaRepository<LedgerTransactionJpa, Long> {
    
    Optional<LedgerTransactionJpa> findByReferenceAndEnvironment(String reference, ApiEnvironment environment);
    
    List<LedgerTransactionJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment, Pageable pageable);
    
    @Query("SELECT t FROM LedgerTransactionJpa t WHERE t.organizationId = :organizationId AND t.environment = :environment " +
           "AND (:accountId IS NULL OR t.id IN (SELECT e.transactionId FROM LedgerEntryJpa e WHERE e.accountId = :accountId)) " +
           "AND (CAST(:dateFrom AS timestamp) IS NULL OR t.postedAt >= :dateFrom) " +
           "AND (CAST(:dateTo AS timestamp) IS NULL OR t.postedAt <= :dateTo)")
    Page<LedgerTransactionJpa> findHistory(@Param("organizationId") Long organizationId,
                                           @Param("environment") ApiEnvironment environment,
                                           @Param("accountId") Long accountId,
                                           @Param("dateFrom") ZonedDateTime dateFrom,
                                           @Param("dateTo") ZonedDateTime dateTo,
                                           Pageable pageable);

    @Query("SELECT t FROM LedgerTransactionJpa t WHERE t.environment = :environment AND t.id IN (SELECT e.transactionId FROM LedgerEntryJpa e WHERE e.accountId = :accountId) AND t.postedAt > :postedAt")
    List<LedgerTransactionJpa> findByAccountIdAndEnvironmentAndPostedAtAfter(@Param("accountId") Long accountId, @Param("environment") ApiEnvironment environment, @Param("postedAt") ZonedDateTime postedAt);

    @Query("SELECT t FROM LedgerTransactionJpa t WHERE t.id IN (SELECT e.transactionId FROM LedgerEntryJpa e WHERE e.accountId = :accountId) AND t.postedAt > :postedAt")
    List<LedgerTransactionJpa> findByAccountIdAndPostedAtAfter(@Param("accountId") Long accountId, @Param("postedAt") ZonedDateTime postedAt);
}
