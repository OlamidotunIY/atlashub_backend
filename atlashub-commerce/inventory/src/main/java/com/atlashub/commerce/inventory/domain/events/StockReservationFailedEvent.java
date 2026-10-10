package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReservationFailedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockReservationFailedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            String failureReason,
            ZonedDateTime failedAt
    ) {
    }

    public static StockReservationFailedEvent of(Long reservationId, Long salesOrderId, Long organizationId,
                                                Long outletId, String failureReason) {
        ZonedDateTime now = ZonedDateTime.now();
        return new StockReservationFailedEvent(
                UUID.randomUUID().toString(),
                reservationId,
                now,
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, outletId, failureReason, now)
        );
    }
}
