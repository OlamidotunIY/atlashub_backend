package com.atlashub.anchor.infrastructure.external.anchor.webhook;

import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.exception.InvalidAnchorWebhookSignatureException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnchorWebhookSignatureVerifierTest {

    private static final byte[] PAYLOAD = "{\"hello\": \"world\"}".getBytes(StandardCharsets.UTF_8);
    private static final String SANDBOX_SIGNATURE = "NDlhNDE3NGQ1ODI2ZTUzNWU2NDg1NDVhNWJiNTU3NDU4OGJhZWVjZA==";

    @Test
    void accepts_anchors_documented_hmac_sha1_hex_base64_signature() {
        AnchorWebhookSignatureVerifier verifier = new AnchorWebhookSignatureVerifier(properties());

        verifier.verify(PAYLOAD, SANDBOX_SIGNATURE, AnchorEnvironment.SANDBOX, AnchorWebhookConsumer.COMPLIANCE);
    }

    @Test
    void rejects_an_invalid_signature() {
        AnchorWebhookSignatureVerifier verifier = new AnchorWebhookSignatureVerifier(properties());

        assertThrows(InvalidAnchorWebhookSignatureException.class, () -> verifier.verify(
                PAYLOAD, "invalid", AnchorEnvironment.SANDBOX, AnchorWebhookConsumer.COMPLIANCE
        ));
    }

    private AnchorProperties properties() {
        return new AnchorProperties(
                Duration.ofSeconds(5),
                new AnchorProperties.EnvironmentProperties(
                        URI.create("https://api.sandbox.getanchor.co"), "sandbox-key", subscriptions("1234"), capabilities(), Duration.ofSeconds(2), Duration.ofSeconds(10)
                ),
                new AnchorProperties.EnvironmentProperties(
                        URI.create("https://api.getanchor.co"), "live-key", subscriptions("live-token"), capabilities(), Duration.ofSeconds(2), Duration.ofSeconds(10)
                )
        );
    }

    private AnchorProperties.WebhookSubscriptions subscriptions(String complianceToken) {
        return new AnchorProperties.WebhookSubscriptions(
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://api.atlashub.com/api/v1/webhooks/anchor/sandbox/compliance"), complianceToken),
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://api.atlashub.com/api/v1/webhooks/anchor/sandbox/pay-accounts"), "pay-accounts-token")
        );
    }

    private AnchorProperties.ProgrammeCapabilities capabilities() {
        return new AnchorProperties.ProgrammeCapabilities(true, true, true, true, "fbo-account");
    }
}
