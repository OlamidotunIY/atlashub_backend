package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalesOrderCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<SalesOrderCreatedEvent.Payload> {

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

    public static SalesOrderCreatedEvent of(Long salesOrderId, Long organizationId, Long outletId, List<OrderItemPayload> items) {
        ZonedDateTime now = ZonedDateTime.now();
        return new SalesOrderCreatedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, outletId, items)
        );
    }
}
