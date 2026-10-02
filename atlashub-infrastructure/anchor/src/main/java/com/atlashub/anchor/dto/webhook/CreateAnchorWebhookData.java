package com.atlashub.anchor.dto.webhook;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/** JSON:API request resource for Anchor's webhook-subscription endpoint. */
public record CreateAnchorWebhookData(String type, Attributes attributes) {
    public static final String RESOURCE_TYPE = "Webhook";

    public CreateAnchorWebhookData(Attributes attributes) {
        this(RESOURCE_TYPE, attributes);
    }

    public CreateAnchorWebhookData {
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Webhook resource type must be " + RESOURCE_TYPE);
        }
        Objects.requireNonNull(attributes, "Webhook attributes are required");
    }

    public record Attributes(
            String deliveryMode,
            URI url,
            String token,
            String label,
            boolean supportIncluded,
            List<String> enabledEvents
    ) {
        public Attributes {
            if (!"AtLeastOnce".equals(deliveryMode)) {
                throw new IllegalArgumentException("Anchor webhooks must use AtLeastOnce delivery");
            }
            Objects.requireNonNull(url, "Webhook URL is required");
            if (!"https".equalsIgnoreCase(url.getScheme()) || url.getHost() == null) {
                throw new IllegalArgumentException("Webhook URL must be an absolute HTTPS URL");
            }
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Webhook token is required");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Webhook label is required");
            }
            if (!supportIncluded) {
                throw new IllegalArgumentException("Anchor webhooks must enable included resources");
            }
            enabledEvents = List.copyOf(Objects.requireNonNull(enabledEvents, "Enabled events are required"));
            if (enabledEvents.isEmpty() || enabledEvents.stream().anyMatch(event -> event == null || event.isBlank())) {
                throw new IllegalArgumentException("At least one valid Anchor event is required");
            }
        }
    }
}
