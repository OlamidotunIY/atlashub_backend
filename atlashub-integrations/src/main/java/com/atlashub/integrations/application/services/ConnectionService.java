package com.atlashub.integrations.application.services;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.events.ProviderConnectionActivated;
import com.atlashub.integrations.domain.events.ProviderConnectionRequested;
import com.atlashub.integrations.domain.repositories.ConnectionRepository;
import com.atlashub.integrations.application.port.ProviderCatalogPort;
import com.atlashub.integrations.application.port.ConnectionVerifier;
import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionService {
    public record ConnectedAuthorization(ProviderConnection connection, ProviderAuthorization authorization) {
    }

    private final ConnectionRepository repository;
    private final CredentialVaultPort vault;
    private final ProviderCatalogPort catalog;
    private final Map<Provider, ConnectionVerifier> verifiers = new EnumMap<>(Provider.class);
    private final DomainEventPublisher events;

    @Autowired
    public ConnectionService(ConnectionRepository repository, CredentialVaultPort vault, ProviderCatalogPort catalog,
                             List<ConnectionVerifier> verifiers, DomainEventPublisher events) {
        this.repository = repository;
        this.vault = vault;
        this.catalog = catalog;
        this.events = events;
        for (ConnectionVerifier verifier : verifiers)
            if (this.verifiers.put(verifier.provider(), verifier) != null)
                throw new IllegalStateException("Multiple connection verifiers registered for " + verifier.provider());
    }

    ConnectionService(ConnectionRepository repository, CredentialVaultPort vault, ProviderCatalogPort catalog) {
        this(repository, vault, catalog, List.of(), null);
    }

    ConnectionService(ConnectionRepository repository, CredentialVaultPort vault, ProviderCatalogPort catalog,
                      List<ConnectionVerifier> verifiers) {
        this(repository, vault, catalog, verifiers, null);
    }

    @Transactional
    public ConnectedAuthorization connectCredential(Long organizationId, Provider provider, String merchantCountry,
                                                    String credential) {
        if (organizationId == null) throw new IllegalArgumentException("Select an organization first");
        ApiEnvironment environment = detectEnvironment(provider, credential);
        String country = merchantCountry.toUpperCase(Locale.ROOT);
        if (catalog.availableIn(country, environment).stream().noneMatch(o -> o.provider() == provider))
            throw new IllegalArgumentException("Provider is unavailable for this merchant country");
        ProviderConnection connection = repository.findConnection(organizationId, provider, country).orElseGet(
                () -> repository.saveConnection(new ProviderConnection(null, organizationId, provider, country,
                        ProviderConnection.Status.CONNECTED)));
        ProviderAuthorization previous = repository.findAuthorization(connection.id(), environment).orElse(null);
        String credentialReference = vault.store(credential);
        ProviderAuthorization pending = repository.saveAuthorization(
                new ProviderAuthorization(previous == null ? null : previous.id(), connection.id(), environment, null,
                        ProviderAuthorization.Status.PENDING_VERIFICATION, ProviderAuthorization.Health.UNKNOWN,
                        Set.of(), credentialReference));
        if (previous != null) vault.delete(previous.credentialReference());
        publish(new ProviderConnectionRequested(UUID.randomUUID().toString(), pending.id(), ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new ProviderConnectionRequested.Payload(organizationId, environment.name(), provider.name(), country)));
        return new ConnectedAuthorization(connection, pending);
    }

    public ProviderAuthorization connect(Long org, ApiEnvironment expected, Provider provider, String country,
                                         String credential) {
        ConnectedAuthorization result = connectCredential(org, provider, country, credential);
        if (result.authorization().environment() != expected)
            throw new IllegalArgumentException("Credential does not match the expected environment");
        return result.authorization();
    }

    public List<ConnectedAuthorization> list(Long org) {
        if (org == null) throw new IllegalArgumentException("Select an organization first");
        return repository.findConnections(org).stream()
                .flatMap(c -> repository.findAuthorizations(c.id()).stream().map(a -> new ConnectedAuthorization(c, a)))
                .toList();
    }

    public List<ConnectedAuthorization> list(Long org, ApiEnvironment env) {
        return list(org).stream().filter(v -> v.authorization().environment() == env).toList();
    }

    @Transactional
    public ConnectedAuthorization verify(Long org, Long authorizationId) {
        ProviderAuthorization authorization = repository.findAuthorization(authorizationId)
                .orElseThrow(() -> new IllegalArgumentException("Provider authorization not found"));
        ProviderConnection connection =
                repository.findConnection(authorization.connectionId()).filter(c -> c.organizationId().equals(org))
                        .orElseThrow(() -> new IllegalArgumentException("Provider connection not found"));
        if (authorization.status() == ProviderAuthorization.Status.ACTIVE)
            return new ConnectedAuthorization(connection, authorization);
        ConnectionVerifier verifier = verifiers.get(connection.provider());
        if (verifier == null)
            throw new IllegalStateException("Connection verification is unavailable for " + connection.provider());
        Set<Capability> documented =
                catalog.availableIn(connection.merchantCountry(), authorization.environment()).stream()
                        .filter(o -> o.provider() == connection.provider()).flatMap(o -> o.capabilities().stream())
                        .collect(Collectors.toUnmodifiableSet());
        ConnectionVerifier.Verification result =
                verifier.verify(authorization.environment(), connection.merchantCountry(),
                        vault.read(authorization.credentialReference()), documented);
        if (!documented.containsAll(result.activeCapabilities()))
            throw new IllegalStateException("Provider returned capabilities outside the reviewed catalog");
        ProviderAuthorization active = repository.saveAuthorization(
                authorization.verified(result.externalMerchantId(), result.activeCapabilities()));
        publish(new ProviderConnectionActivated(UUID.randomUUID().toString(), active.id(), ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new ProviderConnectionActivated.Payload(connection.organizationId(), active.environment().name(),
                        connection.provider().name(), connection.merchantCountry(), active.externalMerchantId(),
                        active.activeCapabilities().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()))));
        return new ConnectedAuthorization(connection, active);
    }

    public ProviderAuthorization verify(Long org, ApiEnvironment expected, Long id) {
        ConnectedAuthorization value = verify(org, id);
        if (value.authorization().environment() != expected)
            throw new IllegalArgumentException("Provider authorization belongs to another environment");
        return value.authorization();
    }

    @Transactional
    public void disconnect(Long org, Long connectionId) {
        ProviderConnection c = repository.findConnection(connectionId).filter(v -> v.organizationId().equals(org))
                .orElseThrow(() -> new IllegalArgumentException("Provider connection not found"));
        repository.findAuthorizations(c.id()).forEach(a -> {
            vault.delete(a.credentialReference());
            repository.saveAuthorization(a.revoked());
        });
        repository.saveConnection(new ProviderConnection(c.id(), c.organizationId(), c.provider(), c.merchantCountry(),
                ProviderConnection.Status.DISCONNECTED));
    }

    public List<ConnectedAuthorization> eligible(Long org, ApiEnvironment env, String currency, Capability capability) {
        return list(org, env).stream().filter(v -> v.connection().status() == ProviderConnection.Status.CONNECTED &&
                v.authorization().eligible(capability) &&
                catalog.supports(v.connection().provider(), v.connection().merchantCountry(), env, currency,
                        capability)).toList();
    }

    private ApiEnvironment detectEnvironment(Provider provider, String credential) {
        if (credential == null || credential.isBlank()) throw new IllegalArgumentException("Credential is required");
        if (provider == Provider.PAYSTACK) {
            if (credential.startsWith("sk_test_") || credential.startsWith("rk_test_")) return ApiEnvironment.TEST;
            if (credential.startsWith("sk_live_") || credential.startsWith("rk_live_")) return ApiEnvironment.LIVE;
        }
        if (provider == Provider.STRIPE) {
            if (credential.startsWith("sk_test_") || credential.startsWith("rk_test_") ||
                    credential.startsWith("oauth_test_")) return ApiEnvironment.TEST;
            if (credential.startsWith("sk_live_") || credential.startsWith("rk_live_") ||
                    credential.startsWith("oauth_live_")) return ApiEnvironment.LIVE;
        }
        if (provider == Provider.ANCHOR) {
            String value = credential.toLowerCase(Locale.ROOT);
            if (value.contains("_test_")) return ApiEnvironment.TEST;
            if (value.contains("_prod_")) return ApiEnvironment.LIVE;
        }
        throw new IllegalArgumentException("Credential environment could not be determined");
    }

    private void publish(com.atlashub.shared.domain.event.DomainEvent<?> event) {
        if (events != null) events.publish(EnvelopedDomainEvent.wrap(event));
    }
}
