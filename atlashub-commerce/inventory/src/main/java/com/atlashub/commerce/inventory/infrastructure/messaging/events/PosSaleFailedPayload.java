package com.atlashub.commerce.inventory.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleFailedPayload(
        String eventId,
        String eventType,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            String reason
    ) {
    }
}
