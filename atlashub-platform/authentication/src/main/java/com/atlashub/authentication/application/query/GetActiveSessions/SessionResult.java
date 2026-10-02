package com.atlashub.authentication.application.query.GetActiveSessions;

import java.time.ZonedDateTime;

public record SessionResult(
        Long id,
        Long organizationId,
        String environment,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
