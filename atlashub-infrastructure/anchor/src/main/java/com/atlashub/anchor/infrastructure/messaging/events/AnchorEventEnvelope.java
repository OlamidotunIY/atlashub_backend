package com.atlashub.anchor.infrastructure.messaging.events;

import java.util.Objects;

/** Kafka envelope compatible with AtlasHub's shared Kafka listener infrastructure. */
public record AnchorEventEnvelope(
        String eventType,
        String correlationId,
        AnchorWebhookReceivedEvent event
) {
    public AnchorEventEnvelope {
        Objects.requireNonNull(eventType, "Event type is required");
        Objects.requireNonNull(correlationId, "Correlation ID is required");
        Objects.requireNonNull(event, "Anchor event is required");
    }
}
