package com.atlashub.compliance.domain.repositories;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ProviderOnboardingCaseRepository extends Repository<ProviderOnboardingCase> {
    Optional<ProviderOnboardingCase> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, ComplianceProvider provider);

    List<ProviderOnboardingCase> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment);
}
