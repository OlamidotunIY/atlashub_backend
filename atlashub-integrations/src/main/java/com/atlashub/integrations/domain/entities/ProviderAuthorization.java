package com.atlashub.integrations.domain.entities;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.integrations.domain.valueobject.Capability;
import java.util.Set;

public record ProviderAuthorization(Long id, Long connectionId, ApiEnvironment environment,
                                    String externalMerchantId, Status status, Health health,
                                    Set<Capability> activeCapabilities, String credentialReference) {
    public enum Status { PENDING_VERIFICATION, ACTIVE, REVOKED }
    public enum Health { UNKNOWN, HEALTHY, DEGRADED, DOWN }

    public ProviderAuthorization {
        activeCapabilities = activeCapabilities == null ? Set.of() : Set.copyOf(activeCapabilities);
        if (connectionId == null || environment == null || status == null || health == null
                || credentialReference == null || credentialReference.isBlank())
            throw new IllegalArgumentException("Complete provider authorization identity is required");
        if (status == Status.ACTIVE && (externalMerchantId == null || externalMerchantId.isBlank()
                || activeCapabilities.isEmpty()))
            throw new IllegalArgumentException("An active authorization requires merchant identity and capabilities");
        if (status == Status.PENDING_VERIFICATION && !activeCapabilities.isEmpty())
            throw new IllegalArgumentException("Unverified authorizations cannot expose capabilities");
    }

    public boolean eligible(Capability capability) {
        return status == Status.ACTIVE && health == Health.HEALTHY && activeCapabilities.contains(capability);
    }

    public ProviderAuthorization verified(String merchantId, Set<Capability> capabilities) {
        return new ProviderAuthorization(id, connectionId, environment, merchantId, Status.ACTIVE,
                Health.HEALTHY, capabilities, credentialReference);
    }

    public ProviderAuthorization revoked() {
        return new ProviderAuthorization(id, connectionId, environment, externalMerchantId, Status.REVOKED,
                Health.DOWN, Set.of(), credentialReference);
    }
}
