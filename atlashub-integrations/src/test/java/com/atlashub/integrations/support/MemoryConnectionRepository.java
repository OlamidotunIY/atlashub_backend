package com.atlashub.integrations.support;

import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MemoryConnectionRepository implements ConnectionRepository {
    private final List<ProviderConnection> connections = new ArrayList<>();
    private final List<ProviderAuthorization> authorizations = new ArrayList<>();

    @Override
    public ProviderConnection saveConnection(ProviderConnection connection) {
        ProviderConnection saved = new ProviderConnection(
                connection.id() == null ? (long) connections.size() + 1 : connection.id(),
                connection.organizationId(), connection.provider(), connection.merchantCountry(), connection.status());
        connections.removeIf(value -> value.id().equals(saved.id()));
        connections.add(saved);
        return saved;
    }

    @Override
    public Optional<ProviderConnection> findConnection(Long id) {
        return connections.stream().filter(value -> value.id().equals(id)).findFirst();
    }

    @Override
    public Optional<ProviderConnection> findConnection(Long organizationId, Provider provider, String country) {
        return connections.stream().filter(value -> value.organizationId().equals(organizationId)
                && value.provider() == provider && value.merchantCountry().equals(country)).findFirst();
    }

    @Override
    public List<ProviderConnection> findConnections(Long organizationId) {
        return connections.stream().filter(value -> value.organizationId().equals(organizationId)).toList();
    }

    @Override
    public ProviderAuthorization saveAuthorization(ProviderAuthorization authorization) {
        ProviderAuthorization saved = new ProviderAuthorization(
                authorization.id() == null ? (long) authorizations.size() + 1 : authorization.id(),
                authorization.connectionId(), authorization.environment(), authorization.externalMerchantId(),
                authorization.status(), authorization.health(), authorization.activeCapabilities(),
                authorization.credentialReference());
        authorizations.removeIf(value -> value.id().equals(saved.id()));
        authorizations.add(saved);
        return saved;
    }

    @Override
    public Optional<ProviderAuthorization> findAuthorization(Long id) {
        return authorizations.stream().filter(value -> value.id().equals(id)).findFirst();
    }

    @Override
    public Optional<ProviderAuthorization> findAuthorization(Long connectionId, ApiEnvironment environment) {
        return authorizations.stream().filter(value -> value.connectionId().equals(connectionId)
                && value.environment() == environment).findFirst();
    }

    @Override
    public Optional<ProviderAuthorization> findAuthorization(ApiEnvironment environment, String merchantId) {
        return authorizations.stream().filter(value -> value.environment() == environment
                && Objects.equals(value.externalMerchantId(), merchantId)).findFirst();
    }

    @Override
    public List<ProviderAuthorization> findAuthorizations(Long connectionId) {
        return authorizations.stream().filter(value -> value.connectionId().equals(connectionId)).toList();
    }
}
