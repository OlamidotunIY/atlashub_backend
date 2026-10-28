package com.atlashub.integrations.infrastructure.persistence.mappers;

import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.infrastructure.persistence.entities.AuthorizationJpa;
import com.atlashub.integrations.infrastructure.persistence.entities.ConnectionJpa;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProviderConnectionMapper {
    public ProviderConnection toDomain(ConnectionJpa row) {
        return new ProviderConnection(row.id, row.organizationId, row.provider, row.merchantCountry, row.status);
    }

    public void copy(ProviderConnection source, ConnectionJpa target) {
        target.organizationId = source.organizationId();
        target.provider = source.provider();
        target.merchantCountry = source.merchantCountry();
        target.status = source.status();
    }

    public ProviderAuthorization toDomain(AuthorizationJpa row) {
        Set<Capability> capabilities = row.activeCapabilities == null || row.activeCapabilities.isBlank()
                ? Set.of() : Arrays.stream(row.activeCapabilities.split(","))
                .map(Capability::valueOf).collect(Collectors.toUnmodifiableSet());
        return new ProviderAuthorization(row.id, row.connectionId, row.environment, row.externalMerchantId,
                row.status, row.health, capabilities, row.credentialReference);
    }

    public void copy(ProviderAuthorization source, AuthorizationJpa target) {
        target.connectionId = source.connectionId();
        target.environment = source.environment();
        target.externalMerchantId = source.externalMerchantId();
        target.status = source.status();
        target.health = source.health();
        target.activeCapabilities = source.activeCapabilities().stream().map(Enum::name)
                .sorted().collect(Collectors.joining(","));
        target.credentialReference = source.credentialReference();
    }
}
