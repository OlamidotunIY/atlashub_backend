package com.atlashub.anchor.infrastructure.external.anchor.messaging.events;

import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResourceIdentifier;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorIncludedResource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.LinkedHashMap;

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
        Map<String, Object> attributes,
        Map<String, AnchorResourceIdentifier> relationships,
        List<AnchorIncludedResource> includedResources
) {
    public AnchorWebhookReceivedEvent {
        requireText(eventId, "Anchor event ID");
        Objects.requireNonNull(environment, "Anchor environment is required");
        Objects.requireNonNull(consumer, "Anchor webhook consumer is required");
        requireText(eventType, "Anchor event type");
        attributes = attributes == null ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        relationships = relationships == null ? Map.of() : Map.copyOf(relationships);
        includedResources = includedResources == null ? List.of() : List.copyOf(includedResources);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
