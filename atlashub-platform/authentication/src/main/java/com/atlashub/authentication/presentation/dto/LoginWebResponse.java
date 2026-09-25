package com.atlashub.authentication.presentation.dto;

import java.time.ZonedDateTime;

public record LoginWebResponse(
        String status,
        String accessToken,
        ZonedDateTime accessTokenExpiresAt,
        String refreshToken,
        ZonedDateTime refreshTokenExpiresAt,
        String message
) {
}
