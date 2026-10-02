package com.atlashub.anchor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * Provider transport settings. Secrets are intentionally required and have no fallback values.
 */
@ConfigurationProperties(prefix = "atlashub.integrations.anchor")
public record AnchorProperties(
        Duration webhookPublishTimeout,
        EnvironmentProperties sandbox,
        EnvironmentProperties live
) {

    public AnchorProperties {
        validateTimeout(webhookPublishTimeout, "webhookPublishTimeout");
        Objects.requireNonNull(sandbox, "Anchor sandbox configuration is required");
        Objects.requireNonNull(live, "Anchor live configuration is required");
    }

    public EnvironmentProperties forEnvironment(AnchorEnvironment environment) {
        return switch (Objects.requireNonNull(environment, "Anchor environment is required")) {
            case SANDBOX -> sandbox;
            case LIVE -> live;
        };
    }

    public record EnvironmentProperties(
            URI baseUrl,
            String apiKey,
            WebhookSubscriptions webhooks,
            Duration connectTimeout,
            Duration readTimeout
    ) {

        public EnvironmentProperties {
            Objects.requireNonNull(baseUrl, "Anchor base URL is required");
            if (!"https".equalsIgnoreCase(baseUrl.getScheme()) || baseUrl.getHost() == null) {
                throw new IllegalArgumentException("Anchor base URL must be an absolute HTTPS URL");
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("Anchor API key is required");
            }
            Objects.requireNonNull(webhooks, "Anchor webhook subscriptions are required");
            AnchorProperties.validateTimeout(connectTimeout, "connectTimeout");
            AnchorProperties.validateTimeout(readTimeout, "readTimeout");
        }
    }

    public record WebhookSubscriptions(
            WebhookSubscriptionProperties compliance,
            WebhookSubscriptionProperties payAccounts
    ) {
        public WebhookSubscriptions {
            Objects.requireNonNull(compliance, "Anchor compliance webhook subscription is required");
            Objects.requireNonNull(payAccounts, "Anchor pay-accounts webhook subscription is required");
        }

        public WebhookSubscriptionProperties forConsumer(AnchorWebhookConsumer consumer) {
            return switch (Objects.requireNonNull(consumer, "Anchor webhook consumer is required")) {
                case COMPLIANCE -> compliance;
                case PAY_ACCOUNTS -> payAccounts;
            };
        }
    }

    public record WebhookSubscriptionProperties(URI callbackUrl, String token) {
        public WebhookSubscriptionProperties {
            Objects.requireNonNull(callbackUrl, "Anchor webhook callback URL is required");
            if (!"https".equalsIgnoreCase(callbackUrl.getScheme()) || callbackUrl.getHost() == null) {
                throw new IllegalArgumentException("Anchor webhook callback URL must be an absolute HTTPS URL");
            }
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Anchor webhook token is required");
            }
        }
    }

    private static void validateTimeout(Duration timeout, String name) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Anchor " + name + " must be positive");
        }
    }
}
