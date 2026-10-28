package com.atlashub.integrations.domain.valueobject;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.Set;

/** Published provider support, not evidence that a particular merchant is enabled. */
public record ProviderOffering(Provider provider, String merchantCountry, ApiEnvironment environment, Set<String> currencies,
                               Set<Capability> capabilities, String sourceUrl) {
    public ProviderOffering {
        currencies = Set.copyOf(currencies);
        capabilities = Set.copyOf(capabilities);
    }

    public boolean offers(Capability capability, String currency) {
        return capabilities.contains(capability) && currencies.contains(currency);
    }
}
