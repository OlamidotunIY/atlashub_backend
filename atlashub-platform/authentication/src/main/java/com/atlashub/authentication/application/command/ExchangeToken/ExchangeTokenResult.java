package com.atlashub.authentication.application.command.ExchangeToken;

public record ExchangeTokenResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        String idToken,
        String scope
) {
}
