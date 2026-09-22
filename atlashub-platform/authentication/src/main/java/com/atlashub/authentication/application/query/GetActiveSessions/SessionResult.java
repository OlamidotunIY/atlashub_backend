package com.atlashub.authentication.application.query.GetActiveSessions;

import java.time.ZonedDateTime;

public record SessionResult(
        String refreshTokenHash,
        ZonedDateTime accessTokenExpiresAt,
        ZonedDateTime refreshTokenExpiresAt,
        Long deviceId,
        Long orgId
) {
}
