package com.atlashub.authentication.application.command.Login;

public record LoginCommand(
        String email,
        String password,
        String deviceFingerprint,
        String ipAddress,
        String userAgent
) {
}

