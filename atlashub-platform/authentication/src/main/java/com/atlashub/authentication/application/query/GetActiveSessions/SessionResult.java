package com.atlashub.authentication.application.query.GetActiveSessions;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

public record SessionResult(
        Long id,
        Long organizationId,
        ApiEnvironment environment,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
