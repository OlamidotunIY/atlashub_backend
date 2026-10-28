package com.atlashub.integrations.infrastructure.external.stripe.adapters;

import com.atlashub.integrations.application.port.StripeWebhookVerifierPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Component
public class StripeWebhookVerifierAdapter implements StripeWebhookVerifierPort {
    private final ObjectMapper mapper;
    @Value("${app.integrations.stripe.test.webhook-secret:}") private String testSecret;
    @Value("${app.integrations.stripe.live.webhook-secret:}") private String liveSecret;

    public StripeWebhookVerifierAdapter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public VerifiedWebhook verify(byte[] payload, String signature) {
        String raw = new String(payload, StandardCharsets.UTF_8);
        ApiEnvironment environment = verifySignature(raw, signature);
        try {
            var event = mapper.readTree(payload);
            String eventId = event.path("id").asText();
            String eventType = event.path("type").asText();
            if (eventId.isBlank() || eventType.isBlank()) {
                throw new IllegalArgumentException("Stripe webhook identity is missing");
            }
            return new VerifiedWebhook(environment, eventId, eventType, event.path("account").asText());
        } catch (java.io.IOException error) {
            throw new IllegalArgumentException("Stripe webhook JSON is invalid", error);
        }
    }

    private ApiEnvironment verifySignature(String payload, String header) {
        if (header == null) throw new IllegalArgumentException("Stripe signature is missing");
        Map<String, List<String>> values = new HashMap<>();
        for (String part : header.split(",")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) values.computeIfAbsent(pair[0], ignored -> new ArrayList<>()).add(pair[1]);
        }
        long timestamp = Long.parseLong(values.getOrDefault("t", List.of("0")).getFirst());
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > 300) {
            throw new IllegalArgumentException("Stripe signature timestamp is expired");
        }
        String signed = timestamp + "." + payload;
        for (ApiEnvironment environment : ApiEnvironment.values()) {
            String secret = environment == ApiEnvironment.LIVE ? liveSecret : testSecret;
            if (secret != null && !secret.isBlank()) {
                String expected = hmac(signed, secret);
                if (values.getOrDefault("v1", List.of()).stream().anyMatch(value -> constant(expected, value))) {
                    return environment;
                }
            }
        }
        throw new IllegalArgumentException("Stripe signature is invalid");
    }

    private String hmac(String value, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private boolean constant(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }
}
