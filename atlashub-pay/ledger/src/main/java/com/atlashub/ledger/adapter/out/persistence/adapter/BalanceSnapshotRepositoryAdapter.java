package com.atlashub.ledger.adapter.out.persistence.adapter;

import com.atlashub.ledger.domain.model.BalanceSnapshot;
import com.atlashub.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlashub.ledger.adapter.out.persistence.entity.BalanceSnapshotJpaEntity;
import com.atlashub.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlashub.ledger.adapter.out.persistence.entity.BalanceSnapshotJpaEntity;
import com.atlashub.ledger.adapter.out.persistence.mapper.BalanceSnapshotMapper;
import com.atlashub.ledger.adapter.out.persistence.repository.SpringDataBalanceSnapshotRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BalanceSnapshotRepositoryAdapter implements BalanceSnapshotRepository {

    private final SpringDataBalanceSnapshotRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final BalanceSnapshotMapper mapper;

    public BalanceSnapshotRepositoryAdapter(SpringDataBalanceSnapshotRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, BalanceSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("balance_snapshot_seq");
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountId(Long accountId) {
        return jpaRepository.findLatestSnapshot(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountIdForUpdate(Long accountId) {
        return jpaRepository.findLatestSnapshotForUpdate(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public BalanceSnapshot save(BalanceSnapshot snapshot) {
        BalanceSnapshotJpaEntity entity = mapper.toEntity(snapshot);
        jpaRepository.save(entity);
        return snapshot;
    }


}
