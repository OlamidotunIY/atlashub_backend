package com.atlashub.accounting.cash.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CashEvacuatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<CashEvacuatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long evacuationId,
            Long organizationId,
            Long outletId,
            Long tillId,
            BigDecimal amount,
            String currency,
            Long evacuatedBy,
            ZonedDateTime confirmedAt
    ) {}

    public static CashEvacuatedEvent of(
            Long evacuationId,
            Long organizationId,
            Long outletId,
            Long tillId,
            BigDecimal amount,
            String currency,
            Long evacuatedBy,
            ZonedDateTime confirmedAt
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new CashEvacuatedEvent(
                UUID.randomUUID().toString(),
                evacuationId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        evacuationId,
                        organizationId,
                        outletId,
                        tillId,
                        amount,
                        currency,
                        evacuatedBy,
                        confirmedAt != null ? confirmedAt : now
                )
        );
    }
}
