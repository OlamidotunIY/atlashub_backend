package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.infrastructure.persistence.entities.BalanceSnapshotJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("accountingGlSpringDataBalanceSnapshotRepository")
public interface SpringDataBalanceSnapshotRepository extends JpaRepository<BalanceSnapshotJpa, Long> {

    Optional<BalanceSnapshotJpa> findTopByAccountIdOrderBySnapshotDateDesc(Long accountId);

    Optional<BalanceSnapshotJpa> findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(Long accountId, LocalDate snapshotDate);

    List<BalanceSnapshotJpa> findByAccountIdAndSnapshotDateBetween(Long accountId, LocalDate from, LocalDate to);
}
