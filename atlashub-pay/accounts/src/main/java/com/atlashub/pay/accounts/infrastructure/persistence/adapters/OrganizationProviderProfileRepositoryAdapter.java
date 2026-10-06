package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.OrganizationProviderProfileJpa;
import com.atlashub.pay.accounts.infrastructure.persistence.mappers.OrganizationProviderProfileMapper;
import com.atlashub.pay.accounts.infrastructure.persistence.repositories.SpringDataOrganizationProviderProfileRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrganizationProviderProfileRepositoryAdapter
        extends JpaBaseRepository<OrganizationProviderProfile, OrganizationProviderProfileJpa>
        implements OrganizationProviderProfileRepository {
    private final SpringDataOrganizationProviderProfileRepository repository;

    public OrganizationProviderProfileRepositoryAdapter(
            SpringDataOrganizationProviderProfileRepository repository,
            OrganizationProviderProfileMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "organization_provider_profile_seq"; }

    @Override
    public Optional<OrganizationProviderProfile> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, PaymentProvider provider) {
        return repository.findByOrganizationIdAndEnvironmentAndProvider(organizationId, environment, provider)
                .map(mapper::toDomain);
    }

    @Override
    public List<OrganizationProviderProfile> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment) {
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment).stream()
                .map(mapper::toDomain).toList();
    }
}
