package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.BalanceSnapshot;
import com.atlashub.accounting.gl.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.BalanceSnapshotMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class BalanceSnapshotRepositoryAdapter
        extends JpaBaseRepository<BalanceSnapshot, BalanceSnapshotJpa>
        implements BalanceSnapshotRepository {

    private final SpringDataBalanceSnapshotRepository springDataRepo;

    public BalanceSnapshotRepositoryAdapter(SpringDataBalanceSnapshotRepository springDataRepo,
                                           BalanceSnapshotMapper mapper,
                                           DomainSequenceGenerator sequenceGenerator,
                                           DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_balance_snapshot_seq";
    }

    @Override
    public Optional<BalanceSnapshot> findLatestByAccountIdAndDate(Long accountId, LocalDate asOf) {
        return springDataRepo
                .findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(accountId, asOf)
                .map(mapper::toDomain);
    }
}
