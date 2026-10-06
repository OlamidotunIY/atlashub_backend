package com.atlashub.authentication.application.command.Logout;

import java.time.ZonedDateTime;

public record LogoutCommand(
        Long userId,
        Long sessionId,
        String accessTokenJti,
        ZonedDateTime accessTokenExpiresAt
) {
}
