package com.atlashub.compliance.infrastructure.persistence.repositories;

import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.infrastructure.persistence.entities.ProviderOnboardingCaseJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProviderOnboardingCaseRepository extends JpaRepository<ProviderOnboardingCaseJpa, Long> {
    Optional<ProviderOnboardingCaseJpa> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, ComplianceProvider provider);

    List<ProviderOnboardingCaseJpa> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment);
}
