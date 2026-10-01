package com.atlashub.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OutletSuspendedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OutletSuspendedEvent.Payload> {
    public record Payload(
            Long organizationId
    ) {}
}
