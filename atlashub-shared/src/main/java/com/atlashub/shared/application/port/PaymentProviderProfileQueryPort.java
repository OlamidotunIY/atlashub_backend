package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.Optional;
import java.util.Set;

/**
 * Synchronous, read-only view owned and implemented by pay:accounts.
 * Consumers must request a capability rather than select an acquiring provider.
 */
public interface PaymentProviderProfileQueryPort {

    Optional<ProviderProfile> findActiveProfile(
            Long organizationId,
            ApiEnvironment environment,
            String capability
    );

    record ProviderProfile(
            Long profileId,
            Long organizationId,
            ApiEnvironment environment,
            String provider,
            String externalMerchantId,
            String settlementAccountReference,
            Set<String> activeCapabilities
    ) {
        public ProviderProfile {
            activeCapabilities = activeCapabilities == null ? Set.of() : Set.copyOf(activeCapabilities);
        }
    }
}
