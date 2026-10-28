package com.atlashub.integrations.domain.repositories;

import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository {
    ProviderConnection saveConnection(ProviderConnection connection);
    Optional<ProviderConnection> findConnection(Long id);
    Optional<ProviderConnection> findConnection(Long organizationId, Provider provider,
                                                String merchantCountry);
    List<ProviderConnection> findConnections(Long organizationId);
    ProviderAuthorization saveAuthorization(ProviderAuthorization authorization);
    Optional<ProviderAuthorization> findAuthorization(Long id);
    Optional<ProviderAuthorization> findAuthorization(Long connectionId, ApiEnvironment environment);
    Optional<ProviderAuthorization> findAuthorization(ApiEnvironment environment, String externalMerchantId);
    List<ProviderAuthorization> findAuthorizations(Long connectionId);
}
