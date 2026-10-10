package com.atlashub.paystack.infrastructure.external.paystack.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "atlashub.integrations.paystack")
public record PaystackProperties(Duration webhookPublishTimeout, EnvironmentProperties test, EnvironmentProperties live) {
    public PaystackProperties {
        requirePositive(webhookPublishTimeout, "webhookPublishTimeout");
        Objects.requireNonNull(test);
        Objects.requireNonNull(live);
    }

    public EnvironmentProperties forEnvironment(PaystackEnvironment environment) {
        return environment == PaystackEnvironment.LIVE ? live : test;
    }

    public record EnvironmentProperties(URI baseUrl, String publicKey, String secretKey, String webhookSecret, Duration connectTimeout,
                                        Duration readTimeout) {
        public EnvironmentProperties {
            Objects.requireNonNull(baseUrl);
            if (!"https".equalsIgnoreCase(baseUrl.getScheme()))
                throw new IllegalArgumentException("Paystack base URL must use HTTPS");
            if (secretKey == null || secretKey.isBlank())
                throw new IllegalArgumentException("Paystack secret key is required");
            if (publicKey == null || publicKey.isBlank())
                throw new IllegalArgumentException("Paystack public key is required");
            if (webhookSecret == null || webhookSecret.isBlank())
                throw new IllegalArgumentException("Paystack webhook secret is required");
            requirePositive(connectTimeout, "connectTimeout");
            requirePositive(readTimeout, "readTimeout");
        }
    }

    private static void requirePositive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative())
            throw new IllegalArgumentException("Paystack " + name + " must be positive");
    }
}
