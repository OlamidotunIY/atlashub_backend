package com.atlashub.ledger.infrastructure.persistence.repository;

import com.atlashub.ledger.infrastructure.persistence.entity.LedgerTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLedgerTransactionRepository extends JpaRepository<LedgerTransactionJpaEntity, Long> {
    @Query("SELECT COUNT(e) > 0 FROM LedgerTransactionJpaEntity e WHERE e.transactionId = :transactionId AND e.sourceSystem = :sourceSystem")
    boolean existsBySourceTransaction(@Param("transactionId") String transactionId, @Param("sourceSystem") String sourceSystem);
}
