package com.atlashub.accounting.gl.domain.repositories;

import com.atlashub.accounting.gl.domain.entities.BalanceSnapshot;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface BalanceSnapshotRepository extends Repository<BalanceSnapshot> {

    Optional<BalanceSnapshot> findLatestByAccountIdAndDate(Long accountId, LocalDate asOf);
}
