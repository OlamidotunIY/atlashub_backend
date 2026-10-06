package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.OrganizationProviderProfileJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrganizationProviderProfileRepository
        extends JpaRepository<OrganizationProviderProfileJpa, Long> {
    Optional<OrganizationProviderProfileJpa> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, PaymentProvider provider);

    List<OrganizationProviderProfileJpa> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment);
}
