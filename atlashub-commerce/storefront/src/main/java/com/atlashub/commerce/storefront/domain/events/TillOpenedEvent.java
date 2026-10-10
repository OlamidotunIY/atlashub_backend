package com.atlashub.commerce.storefront.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TillOpenedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<TillOpenedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long tillSessionId,
            Long organizationId,
            Long outletId,
            Long cashierId,
            BigDecimal openingFloat,
            String currency,
            ZonedDateTime openedAt
    ) {
    }

    public static TillOpenedEvent of(
            Long tillSessionId,
            Long organizationId,
            Long outletId,
            Long cashierId,
            BigDecimal openingFloat,
            String currency
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new TillOpenedEvent(
                UUID.randomUUID().toString(),
                tillSessionId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        tillSessionId,
                        organizationId,
                        outletId,
                        cashierId,
                        openingFloat,
                        currency,
                        now
                )
        );
    }
}
