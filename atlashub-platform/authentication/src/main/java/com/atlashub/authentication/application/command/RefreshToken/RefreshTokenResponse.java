package com.atlashub.authentication.application.command.RefreshToken;

import java.time.ZonedDateTime;

public record RefreshTokenResponse(
        String accessToken,
        ZonedDateTime accessTokenExpiresAt,
        String refreshToken,
        ZonedDateTime refreshTokenExpiresAt
) {
}
