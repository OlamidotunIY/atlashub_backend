package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosSaleCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<PosSaleCompletedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            BigDecimal totalNet,
            String currency,
            String paymentMethod,
            Long cashierId,
            ZonedDateTime completedAt
    ) {
    }

    public static PosSaleCompletedEvent of(
            Long salesOrderId,
            Long organizationId,
            Long outletId,
            BigDecimal totalNet,
            String currency,
            String paymentMethod,
            Long cashierId
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new PosSaleCompletedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        salesOrderId,
                        organizationId,
                        outletId,
                        totalNet,
                        currency,
                        paymentMethod,
                        cashierId,
                        now
                )
        );
    }
}
