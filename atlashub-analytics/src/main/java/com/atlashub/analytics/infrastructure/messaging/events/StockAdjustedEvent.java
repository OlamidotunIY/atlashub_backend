package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockAdjustedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<StockAdjustedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            Long outletId,
            Long productId,
            int previousQty,
            int newQty,
            Integer reorderLevel,
            String reason,
            Long adjustedBy,
            ZonedDateTime adjustedAt
    ) {
    }
}
