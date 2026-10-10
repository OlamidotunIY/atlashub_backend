package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnlineOrderCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OnlineOrderCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long customerId,
            BigDecimal totalNet,
            String currency,
            String deliveryAddress,
            ZonedDateTime createdAt
    ) {
    }

    public static OnlineOrderCreatedEvent of(
            Long salesOrderId,
            Long organizationId,
            Long customerId,
            BigDecimal totalNet,
            String currency,
            String deliveryAddress
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new OnlineOrderCreatedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, customerId, totalNet, currency, deliveryAddress, now)
        );
    }
}
