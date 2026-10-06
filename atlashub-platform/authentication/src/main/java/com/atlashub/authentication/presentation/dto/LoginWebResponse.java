package com.atlashub.authentication.presentation.dto;

import com.atlashub.authentication.domain.valueobject.LoginStatus;

import java.time.ZonedDateTime;

public record LoginWebResponse(
        LoginStatus status,
        String accessToken,
        ZonedDateTime accessTokenExpiresAt,
        String refreshToken,
        ZonedDateTime refreshTokenExpiresAt,
        String message
) {
}
