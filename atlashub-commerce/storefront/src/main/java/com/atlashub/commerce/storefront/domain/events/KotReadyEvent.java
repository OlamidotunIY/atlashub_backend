package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KotReadyEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<KotReadyEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long kotId,
            Long salesOrderId,
            Long outletId,
            Long tableId,
            ZonedDateTime readyAt
    ) {
    }

    public static KotReadyEvent of(
            Long kotId,
            Long salesOrderId,
            Long outletId,
            Long tableId
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new KotReadyEvent(
                UUID.randomUUID().toString(),
                kotId,
                now,
                UUID.randomUUID().toString(),
                new Payload(kotId, salesOrderId, outletId, tableId, now)
        );
    }
}
