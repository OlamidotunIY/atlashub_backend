package com.atlashub.authentication.application.command.ExchangeToken;

public record ExchangeTokenCommand(
        String grantType,
        String code,
        String redirectUri,
        String clientId,
        String clientSecret,
        String codeVerifier,
        String refreshToken,
        String deviceFingerprint,
        String ipAddress,
        String userAgent
) {
}
