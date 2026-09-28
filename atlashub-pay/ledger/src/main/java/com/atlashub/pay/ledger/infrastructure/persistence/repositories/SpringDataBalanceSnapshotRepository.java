package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.BalanceSnapshotJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataBalanceSnapshotRepository extends JpaRepository<BalanceSnapshotJpa, Long> {
    Optional<BalanceSnapshotJpa> findTopByAccountIdOrderBySnapshotAtDesc(Long accountId);
    
    @Query("SELECT b FROM BalanceSnapshotJpa b WHERE b.accountId IN :accountIds AND b.snapshotAt = (SELECT MAX(b2.snapshotAt) FROM BalanceSnapshotJpa b2 WHERE b2.accountId = b.accountId)")
    List<BalanceSnapshotJpa> findAllLatestByAccountIdIn(@Param("accountIds") List<Long> accountIds);
}
