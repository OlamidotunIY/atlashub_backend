package com.atlashub.anchor.client;

import com.atlashub.anchor.configuration.AnchorEnvironment;

import java.util.Map;
import java.util.Objects;

/**
 * Selects the configured Anchor clients without allowing callers to handle credentials or URLs.
 */
public final class AnchorClientRegistry {
    private final Map<AnchorEnvironment, AnchorClients> clientsByEnvironment;

    public AnchorClientRegistry(Map<AnchorEnvironment, AnchorClients> clientsByEnvironment) {
        this.clientsByEnvironment = Map.copyOf(Objects.requireNonNull(clientsByEnvironment, "Clients are required"));
        for (AnchorEnvironment environment : AnchorEnvironment.values()) {
            if (!this.clientsByEnvironment.containsKey(environment)) {
                throw new IllegalArgumentException("Missing Anchor clients for " + environment);
            }
        }
    }

    public AnchorClients forEnvironment(AnchorEnvironment environment) {
        AnchorClients clients = clientsByEnvironment.get(Objects.requireNonNull(environment, "Anchor environment is required"));
        if (clients == null) {
            throw new IllegalArgumentException("No Anchor clients configured for " + environment);
        }
        return clients;
    }
}
