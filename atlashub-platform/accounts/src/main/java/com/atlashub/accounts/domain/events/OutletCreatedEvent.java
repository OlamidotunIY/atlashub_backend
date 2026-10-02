package com.atlashub.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OutletCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OutletCreatedEvent.Payload> {
    public record Payload(
            Long outletId,
            Long organizationId,
            String name,
            String address,
            String city,
            String state,
            String country,
            String currency
    ) {}
}
