package com.atlashub.anchor.infrastructure.external.anchor.webhook;

import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.exception.InvalidAnchorWebhookSignatureException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Verifies Anchor's Base64(HMAC-SHA1-hex(raw-body, webhook-token)) signature format. */
@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorWebhookSignatureVerifier {
    private static final String HMAC_SHA_1 = "HmacSHA1";

    private final AnchorProperties properties;

    public AnchorWebhookSignatureVerifier(AnchorProperties properties) {
        this.properties = properties;
    }

    public void verify(
            byte[] rawBody,
            String signature,
            AnchorEnvironment environment,
            AnchorWebhookConsumer consumer
    ) {
        if (rawBody == null || signature == null || signature.isBlank()) {
            throw new InvalidAnchorWebhookSignatureException();
        }
        String token = properties.forEnvironment(environment).webhooks().forConsumer(consumer).token();
        String expected = signature(rawBody, token);
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                signature.trim().getBytes(StandardCharsets.US_ASCII)
        )) {
            throw new InvalidAnchorWebhookSignatureException();
        }
    }

    private String signature(byte[] rawBody, String token) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_1);
            mac.init(new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), HMAC_SHA_1));
            byte[] digest = mac.doFinal(rawBody);
            return Base64.getEncoder().encodeToString(toLowerHex(digest).getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to verify Anchor webhook signature", exception);
        }
    }

    private String toLowerHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(Character.forDigit((value >>> 4) & 0xF, 16));
            hex.append(Character.forDigit(value & 0xF, 16));
        }
        return hex.toString();
    }
}
