package com.atlashub.auth.application.command;

public record RefreshTokenCommand(
        String refreshToken,
        String ipAddress,
        String userAgent
) {}
