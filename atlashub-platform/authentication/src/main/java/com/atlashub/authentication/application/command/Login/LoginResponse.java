package com.atlashub.authentication.application.command.Login;

import com.atlashub.authentication.domain.valueobject.LoginStatus;

import java.time.ZonedDateTime;

public record LoginResponse(
        LoginStatus status,
        String accessToken,    // null if OTP is required
        ZonedDateTime accessTokenExpiresAt,
        String refreshToken,   // null if OTP is required
        ZonedDateTime refreshTokenExpiresAt,
        String message         // e.g., "OTP sent to your email"
) {
    // Helper factories for clean code in your handler
    public static LoginResponse success(String access, String refresh, ZonedDateTime accessTokenExpiresAt, ZonedDateTime refreshTokenExpiresAt) {
        return new LoginResponse(LoginStatus.SUCCESS, access, accessTokenExpiresAt, refresh, refreshTokenExpiresAt, null);
    }

    public static LoginResponse otpRequired(String message) {
        return new LoginResponse(LoginStatus.OTP_REQUIRED, null, null, null, null, message);
    }
}


