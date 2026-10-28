package com.atlashub.integrations.domain.entities;

import com.atlashub.integrations.domain.valueobject.Provider;

public record ProviderConnection(Long id, Long organizationId, Provider provider,
                                 String merchantCountry, Status status) {
    public enum Status { CONNECTED, DISCONNECTED }

    public ProviderConnection {
        if (organizationId == null || provider == null || merchantCountry == null || merchantCountry.isBlank()
                || status == null) throw new IllegalArgumentException("Complete provider connection identity is required");
        merchantCountry = merchantCountry.toUpperCase(java.util.Locale.ROOT);
    }
}
