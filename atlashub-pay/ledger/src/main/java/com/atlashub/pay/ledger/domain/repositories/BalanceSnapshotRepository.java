package com.atlashub.pay.ledger.domain.repositories;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface BalanceSnapshotRepository extends Repository<BalanceSnapshot> {
    Optional<BalanceSnapshot> findLatestByAccountId(Long accountId);
    List<BalanceSnapshot> findAllLatestByAccountIdIn(List<Long> accountIds);
}
