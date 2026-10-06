package com.atlashub.pay.ledger.infrastructure.persistence.adapters;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.BalanceSnapshotMapper;
import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BalanceSnapshotRepositoryAdapter implements BalanceSnapshotRepository {

    private final SpringDataBalanceSnapshotRepository springDataRepo;
    private final BalanceSnapshotMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public BalanceSnapshotRepositoryAdapter(SpringDataBalanceSnapshotRepository springDataRepo,
                                            BalanceSnapshotMapper mapper,
                                            DomainSequenceGenerator sequenceGenerator) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("balance_snapshot_seq");
    }

    @Override
    @Transactional
    public BalanceSnapshot save(BalanceSnapshot entity) {
        BalanceSnapshotJpa record = mapper.toPersistence(entity);
        BalanceSnapshotJpa saved = springDataRepo.save(record);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BalanceSnapshot> findById(Long id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public List<BalanceSnapshot> findAll() {
        return springDataRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountId(Long accountId) {
        return springDataRepo.findTopByAccountIdOrderBySnapshotAtDesc(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public List<BalanceSnapshot> findAllLatestByAccountIdIn(List<Long> accountIds) {
        return springDataRepo.findAllLatestByAccountIdIn(accountIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
