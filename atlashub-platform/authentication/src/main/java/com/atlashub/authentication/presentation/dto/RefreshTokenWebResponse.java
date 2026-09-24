package com.atlashub.authentication.presentation.dto;

import java.time.ZonedDateTime;

public record RefreshTokenWebResponse(
        String accessToken,
        ZonedDateTime accessTokenExpiresAt,
        String refreshToken,
        ZonedDateTime refreshTokenExpiresAt
) {
}
