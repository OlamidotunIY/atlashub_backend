package com.atlashub.accounting.cash.infrastructure.persistence.adapters;

import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.repositories.CashEvacuationRepository;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.CashEvacuationJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.CashEvacuationMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataCashEvacuationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CashEvacuationRepositoryAdapter
        extends JpaBaseRepository<CashEvacuation, CashEvacuationJpaEntity>
        implements CashEvacuationRepository {

    private final SpringDataCashEvacuationRepository springDataRepository;

    public CashEvacuationRepositoryAdapter(SpringDataCashEvacuationRepository springDataRepository,
                                           CashEvacuationMapper mapper,
                                           DomainSequenceGenerator sequenceGenerator,
                                           DomainEventPublisher eventPublisher) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepository = springDataRepository;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_cash_evacuation_seq";
    }

    @Override
    public List<CashEvacuation> findByOrganizationId(Long organizationId) {
        return springDataRepository.findByOrganizationIdOrderByEvacuatedAtDesc(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CashEvacuation> findByOrganizationIdAndStatus(Long organizationId, EvacuationStatus status) {
        return springDataRepository
                .findByOrganizationIdAndStatusOrderByEvacuatedAtDesc(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
