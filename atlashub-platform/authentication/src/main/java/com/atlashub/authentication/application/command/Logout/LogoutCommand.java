package com.atlashub.authentication.application.command.Logout;

import java.time.ZonedDateTime;

public record LogoutCommand(
        String refreshToken,
        String accessTokenJti,          // The unique ID inside the JWT payload
        ZonedDateTime accessTokenExpiresAt // When the JWT naturally expires
) {
}
