package com.atlashub.commerce.inventory.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerReturnShipmentReceivedPayload(
        String eventId,
        String eventType,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long returnShipmentId,
            Long returnId,
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            List<ReceivedItem> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceivedItem(
            Long productId,
            int quantity
    ) {
    }
}
