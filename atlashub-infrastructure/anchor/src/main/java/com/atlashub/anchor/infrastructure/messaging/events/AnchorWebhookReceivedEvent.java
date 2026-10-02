package com.atlashub.anchor.infrastructure.messaging.events;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.dto.common.AnchorResourceIdentifier;

import java.util.Map;
import java.util.Objects;

/**
 * A verified Anchor webhook event. Consumers use this shared provider event directly and translate
 * it into module-owned commands and business events.
 */
public record AnchorWebhookReceivedEvent(
        String eventId,
        AnchorEnvironment environment,
        AnchorWebhookConsumer consumer,
        String eventType,
        String occurredAt,
        Map<String, AnchorResourceIdentifier> relationships
) {
    public AnchorWebhookReceivedEvent {
        requireText(eventId, "Anchor event ID");
        Objects.requireNonNull(environment, "Anchor environment is required");
        Objects.requireNonNull(consumer, "Anchor webhook consumer is required");
        requireText(eventType, "Anchor event type");
        relationships = relationships == null ? Map.of() : Map.copyOf(relationships);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
