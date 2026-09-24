package com.atlashub.shared.domain.event;

import java.time.ZonedDateTime;
import java.util.Map;

public record WebhookDeliveryRequestedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Payload payload
) {

    public record Payload(
            String endpointUrl,
            String payloadJson,
            Map<String, String> headers
    ) {}
}
