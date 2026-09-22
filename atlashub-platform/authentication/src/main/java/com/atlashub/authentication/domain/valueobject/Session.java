package com.atlashub.authentication.domain.valueobject;

import java.time.ZonedDateTime;

public record Session(
        Long authAccountId,
        String refreshTokenHash,
        String accessTokenJti,
        ZonedDateTime accessTokenExpiresAt,
        ZonedDateTime refreshTokenExpiresAt,
        Long deviceId,
        Long orgId
) {
}
