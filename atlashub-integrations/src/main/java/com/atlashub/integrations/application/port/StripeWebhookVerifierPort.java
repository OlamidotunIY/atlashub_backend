package com.atlashub.integrations.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

public interface StripeWebhookVerifierPort {
    VerifiedWebhook verify(byte[] payload, String signature);

    record VerifiedWebhook(ApiEnvironment environment, String eventId, String eventType,
                           String externalAccountId) {}
}
