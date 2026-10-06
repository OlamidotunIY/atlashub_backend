package com.atlashub.authentication.application.command.Login;

import com.atlashub.shared.application.security.ApiEnvironment;

public record LoginCommand(
        String email,
        String password,
        ApiEnvironment environment,
        String deviceFingerprint,
        String ipAddress,
        String userAgent
) {
}
