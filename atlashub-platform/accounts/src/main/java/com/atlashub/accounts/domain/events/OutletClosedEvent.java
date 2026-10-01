package com.atlashub.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OutletClosedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OutletClosedEvent.Payload> {
    public record Payload(
            Long organizationId
    ) {}
}
