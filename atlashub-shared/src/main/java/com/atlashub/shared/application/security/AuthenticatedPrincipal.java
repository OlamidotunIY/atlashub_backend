package com.atlashub.shared.application.security;

import java.time.ZonedDateTime;

public record AuthenticatedPrincipal(
        Long userId,
        Long activeOrganizationId,
        String environment,
        String sessionId,
        String tokenId,
        ZonedDateTime tokenExpiresAt
) {
    public AuthenticatedPrincipal {
        environment = ApiEnvironment.parse(environment).name();
    }

    public ApiEnvironment apiEnvironment() {
        return ApiEnvironment.parse(environment);
    }
}
