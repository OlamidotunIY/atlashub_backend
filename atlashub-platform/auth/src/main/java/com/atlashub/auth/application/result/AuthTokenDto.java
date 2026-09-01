package com.atlashub.auth.application.result;

import java.time.ZonedDateTime;

public record AuthTokenDto(
        String accessToken,
        String refreshToken,
        ZonedDateTime accessExpiresAt,
        ZonedDateTime refreshExpiresAt
) {}
