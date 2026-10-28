package com.atlashub.integrations.domain.entities;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

public record AuthorizationSession(Long id, String stateHash, Long organizationId,
                                   ApiEnvironment environment, String merchantCountry,
                                   ZonedDateTime expiresAt, boolean consumed) {
    public AuthorizationSession {
        if (stateHash == null || stateHash.isBlank() || organizationId == null || environment == null
                || merchantCountry == null || merchantCountry.isBlank() || expiresAt == null) {
            throw new IllegalArgumentException("Complete authorization session data is required");
        }
    }

    public AuthorizationSession consume() {
        return new AuthorizationSession(id, stateHash, organizationId, environment, merchantCountry, expiresAt, true);
    }
}
