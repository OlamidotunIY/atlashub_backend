package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface OrganizationProviderProfileRepository extends Repository<OrganizationProviderProfile> {
    Optional<OrganizationProviderProfile> findByOrganizationIdAndEnvironmentAndProvider(
            Long organizationId, ApiEnvironment environment, PaymentProvider provider);

    List<OrganizationProviderProfile> findByOrganizationIdAndEnvironment(
            Long organizationId, ApiEnvironment environment);

    List<OrganizationProviderProfile> findByEnvironmentAndProviderAndStatus(
            ApiEnvironment environment, PaymentProvider provider, ProviderProfileStatus status);
}
