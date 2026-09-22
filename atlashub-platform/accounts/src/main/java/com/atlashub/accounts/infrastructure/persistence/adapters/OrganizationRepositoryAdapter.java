package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.accounts.infrastructure.persistence.mappers.OrganizationMapper;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOrganizationRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganizationRepositoryAdapter extends JpaBaseRepository<Organization, OrganizationJPA> implements OrganizationRepository {

    private final SpringDataOrganizationRepository springDataRepo;

    protected OrganizationRepositoryAdapter(SpringDataOrganizationRepository springDataRepository, OrganizationMapper mapper, DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepository;
    }

    @Override
    protected String getSequenceName() {
        return "organization_seq";
    }

    @Override
    public List<Organization> findAllByIds(List<Long> ids) {
        return springDataRepo.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
