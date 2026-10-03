package com.atlashub.compliance.infrastructure.persistence.adapters;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.domain.repositories.ProviderOnboardingCaseRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.infrastructure.persistence.entities.ProviderOnboardingCaseJpa;
import com.atlashub.compliance.infrastructure.persistence.mappers.ProviderOnboardingCaseMapper;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataProviderOnboardingCaseRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProviderOnboardingCaseRepositoryAdapter
        extends JpaBaseRepository<ProviderOnboardingCase, ProviderOnboardingCaseJpa>
        implements ProviderOnboardingCaseRepository {
    private final SpringDataProviderOnboardingCaseRepository repository;

    public ProviderOnboardingCaseRepositoryAdapter(
            SpringDataProviderOnboardingCaseRepository repository,
            ProviderOnboardingCaseMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(repository, mapper, sequenceGenerator, eventPublisher);
        this.repository = repository;
    }

    @Override protected String getSequenceName() { return "provider_onboarding_case_seq"; }

    @Override
    public Optional<ProviderOnboardingCase> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, ComplianceProvider provider) {
        return repository.findByOrganizationIdAndEnvironmentAndProvider(organizationId, environment, provider)
                .map(mapper::toDomain);
    }

    @Override
    public List<ProviderOnboardingCase> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment) {
        return repository.findByOrganizationIdAndEnvironment(organizationId, environment).stream()
                .map(mapper::toDomain).toList();
    }
}
