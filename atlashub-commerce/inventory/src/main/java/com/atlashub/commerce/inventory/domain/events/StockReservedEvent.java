package com.atlashub.commerce.inventory.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReservedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockReservedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            List<ReservedItemDto> items,
            ZonedDateTime reservedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReservedItemDto(
            Long productId,
            int quantity
    ) {
    }

    public static StockReservedEvent of(Long reservationId, Long salesOrderId, Long organizationId,
                                        Long outletId, List<ReservedItemDto> items) {
        ZonedDateTime now = ZonedDateTime.now();
        return new StockReservedEvent(
                UUID.randomUUID().toString(),
                reservationId,
                now,
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, outletId, items, now)
        );
    }
}
