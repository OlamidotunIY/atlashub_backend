package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KitchenOrderTicketCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<KitchenOrderTicketCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long kotId,
            Long salesOrderId,
            Long outletId,
            Long tableId,
            List<KotItemPayload> items,
            ZonedDateTime createdAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KotItemPayload(
            Long productId,
            String name,
            int quantity
    ) {
    }

    public static KitchenOrderTicketCreatedEvent of(
            Long kotId,
            Long salesOrderId,
            Long outletId,
            Long tableId,
            List<KotItemPayload> items
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new KitchenOrderTicketCreatedEvent(
                UUID.randomUUID().toString(),
                kotId,
                now,
                UUID.randomUUID().toString(),
                new Payload(kotId, salesOrderId, outletId, tableId, items, now)
        );
    }
}
