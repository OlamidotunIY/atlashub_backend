package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record CustomerCreated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<CustomerCreated.Payload> {
    public record Payload(
        Long integration,
        String email,
        String firstName,
        String lastName
    ) {}
}
