package com.atlashub.authentication.application.query.GetActiveSessions;

import java.time.ZonedDateTime;

public record SessionResult(
        String token,
        ZonedDateTime expiresAt,
        String ipAddress,
        String userAgent
) {
}
