package com.atlashub.commerce.inventory.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalesOrderCreatedPayload(
        String eventId,
        String eventType,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            List<OrderItemPayload> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItemPayload(
            Long productId,
            Long variantId,
            int quantity
    ) {
    }
}
