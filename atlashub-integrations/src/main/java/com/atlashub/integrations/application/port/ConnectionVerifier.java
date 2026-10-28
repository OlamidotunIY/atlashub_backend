package com.atlashub.integrations.application.port;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.Set;

public interface ConnectionVerifier {
    Provider provider();
    Verification verify(ApiEnvironment environment, String merchantCountry, String credential,
                        Set<Capability> documentedCapabilities);

    record Verification(String externalMerchantId, Set<Capability> activeCapabilities) {
        public Verification {
            if (externalMerchantId == null || externalMerchantId.isBlank())
                throw new IllegalArgumentException("Verified merchant identity is required");
            activeCapabilities = activeCapabilities == null ? Set.of() : Set.copyOf(activeCapabilities);
            if (activeCapabilities.isEmpty())
                throw new IllegalArgumentException("Provider did not confirm any capabilities");
        }
    }
}
