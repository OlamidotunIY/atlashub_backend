package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record CustomerProfileUpdated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<CustomerProfileUpdated.Payload> {
    public record Payload(
        Long integration,
        String firstName,
        String lastName,
        String phone
    ) {}
}
