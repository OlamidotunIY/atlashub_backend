package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LayawayCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LayawayCreatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long salesOrderId,
            Long organizationId,
            Long depositId,
            BigDecimal depositAmount,
            BigDecimal balanceRemaining,
            String currency,
            ZonedDateTime createdAt
    ) {
    }

    public static LayawayCreatedEvent of(
            Long salesOrderId,
            Long organizationId,
            Long depositId,
            BigDecimal depositAmount,
            BigDecimal balanceRemaining,
            String currency
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new LayawayCreatedEvent(
                UUID.randomUUID().toString(),
                salesOrderId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        salesOrderId,
                        organizationId,
                        depositId,
                        depositAmount,
                        balanceRemaining,
                        currency,
                        now
                )
        );
    }
}
