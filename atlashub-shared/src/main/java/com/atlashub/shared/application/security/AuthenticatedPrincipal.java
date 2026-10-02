package com.atlashub.shared.application.security;

public record AuthenticatedPrincipal(
        Long userId,
        Long activeOrganizationId,
        String environment,
        String sessionId
) {
}
