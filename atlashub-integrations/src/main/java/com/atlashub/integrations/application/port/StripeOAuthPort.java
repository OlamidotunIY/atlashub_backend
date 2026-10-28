package com.atlashub.integrations.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

public interface StripeOAuthPort {
    String createAuthorizationUrl(ApiEnvironment environment, String merchantCountry, String state);
    Token exchange(ApiEnvironment environment, String authorizationCode);

    record Token(String accessToken, String externalAccountId, boolean live) {
        public Token {
            if (accessToken == null || accessToken.isBlank() || externalAccountId == null || externalAccountId.isBlank()) {
                throw new IllegalArgumentException("Stripe authorization token and account are required");
            }
        }
    }
}
