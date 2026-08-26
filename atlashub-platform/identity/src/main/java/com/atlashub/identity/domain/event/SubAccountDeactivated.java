package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record SubAccountDeactivated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<SubAccountDeactivated.Payload> {
    public record Payload(String merchantId) {}
}
