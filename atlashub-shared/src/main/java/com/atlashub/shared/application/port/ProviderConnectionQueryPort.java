package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.List;
import java.util.Set;

/**
 * Read-only view of verified merchant provider connections. The integrations
 * bounded context owns the connection and credential data.
 */
public interface ProviderConnectionQueryPort {
    List<ProviderConnection> findEligible(Long organizationId, ApiEnvironment environment,
                                          String currency, String capability);

    /** connectionId identifies the environment-specific authorization used for routing. */
    record ProviderConnection(Long connectionId, String provider, String externalMerchantId,
                              Set<String> activeCapabilities) {
        public ProviderConnection {
            activeCapabilities = activeCapabilities == null ? Set.of() : Set.copyOf(activeCapabilities);
        }
    }
}
