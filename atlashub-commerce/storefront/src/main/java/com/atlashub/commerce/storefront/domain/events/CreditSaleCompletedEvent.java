package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreditSaleCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<CreditSaleCompletedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long customerId,
            BigDecimal totalNet,
            String currency,
            ZonedDateTime completedAt
    ) {
    }

    public static CreditSaleCompletedEvent of(
            Long salesOrderId,
            Long organizationId,
            Long customerId,
            BigDecimal totalNet,
            String currency
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new CreditSaleCompletedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(salesOrderId, organizationId, customerId, totalNet, currency, now)
        );
    }
}
