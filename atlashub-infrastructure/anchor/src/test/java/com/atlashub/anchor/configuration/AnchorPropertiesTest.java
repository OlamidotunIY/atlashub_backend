package com.atlashub.anchor.configuration;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnchorPropertiesTest {

    @Test
    void selects_credentials_for_each_environment() {
        AnchorProperties properties = properties();

        assertEquals("sandbox-key", properties.forEnvironment(AnchorEnvironment.SANDBOX).apiKey());
        assertEquals("live-key", properties.forEnvironment(AnchorEnvironment.LIVE).apiKey());
    }

    @Test
    void rejects_missing_api_key() {
        assertThrows(IllegalArgumentException.class, () -> new AnchorProperties.EnvironmentProperties(
                URI.create("https://api.sandbox.getanchor.co"), " ", subscriptions(), Duration.ofSeconds(2), Duration.ofSeconds(10)
        ));
    }

    @Test
    void rejects_non_https_base_url() {
        assertThrows(IllegalArgumentException.class, () -> new AnchorProperties.EnvironmentProperties(
                URI.create("http://api.sandbox.getanchor.co"), "key", subscriptions(), Duration.ofSeconds(2), Duration.ofSeconds(10)
        ));
    }

    private AnchorProperties properties() {
        return new AnchorProperties(
                Duration.ofSeconds(5),
                new AnchorProperties.EnvironmentProperties(
                        URI.create("https://api.sandbox.getanchor.co"), "sandbox-key", subscriptions(), Duration.ofSeconds(2), Duration.ofSeconds(10)
                ),
                new AnchorProperties.EnvironmentProperties(
                        URI.create("https://api.getanchor.co"), "live-key", subscriptions(), Duration.ofSeconds(2), Duration.ofSeconds(10)
                )
        );
    }

    private AnchorProperties.WebhookSubscriptions subscriptions() {
        return new AnchorProperties.WebhookSubscriptions(
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://sandbox.atlashub.com/api/v1/webhooks/anchor/sandbox/compliance"), "compliance-token"),
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://sandbox.atlashub.com/api/v1/webhooks/anchor/sandbox/pay-accounts"), "pay-accounts-token")
        );
    }
}
