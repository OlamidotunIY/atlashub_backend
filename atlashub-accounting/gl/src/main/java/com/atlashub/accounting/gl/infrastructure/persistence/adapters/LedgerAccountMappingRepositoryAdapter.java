package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.LedgerAccountMappingJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.LedgerAccountMappingMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataLedgerAccountMappingRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LedgerAccountMappingRepositoryAdapter
        extends JpaBaseRepository<LedgerAccountMapping, LedgerAccountMappingJpa>
        implements LedgerAccountMappingRepository {

    private final SpringDataLedgerAccountMappingRepository springDataRepo;

    public LedgerAccountMappingRepositoryAdapter(SpringDataLedgerAccountMappingRepository springDataRepo,
                                                LedgerAccountMappingMapper mapper,
                                                DomainSequenceGenerator sequenceGenerator,
                                                DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_ledger_mapping_seq";
    }

    @Override
    public Optional<LedgerAccountMapping> findByOrganizationIdAndSourceSystem(Long organizationId, SourceSystem sourceSystem) {
        return springDataRepo.findByOrganizationIdAndSourceSystem(organizationId, sourceSystem).map(mapper::toDomain);
    }

    @Override
    public List<LedgerAccountMapping> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
