package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.OrganizationBankingProfileJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.OrganizationBankingProfileMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataOrganizationBankingProfileRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationBankingProfileRepositoryAdapter
        extends JpaBaseRepository<OrganizationBankingProfile, OrganizationBankingProfileJpa>
        implements OrganizationBankingProfileRepository {
    private final SpringDataOrganizationBankingProfileRepository repository;

    public OrganizationBankingProfileRepositoryAdapter(
            SpringDataOrganizationBankingProfileRepository repository,
            OrganizationBankingProfileMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "organization_banking_profile_seq"; }

    @Override public Optional<OrganizationBankingProfile> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }
}
