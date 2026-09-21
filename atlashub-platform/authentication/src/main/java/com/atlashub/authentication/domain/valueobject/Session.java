package com.atlashub.authentication.domain.valueobject;

import java.time.ZonedDateTime;

public record Session(
        Long authAccountId,
        String refreshTokenHash,
        ZonedDateTime accessTokenExpiresAt,
        ZonedDateTime refreshTokenExpiresAt,
        Long deviceId,
        Long orgId
) {
}
