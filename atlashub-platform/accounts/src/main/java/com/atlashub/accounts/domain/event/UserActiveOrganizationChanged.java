package com.atlashub.accounts.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record UserActiveOrganizationChanged(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    String correlationId,
    Payload payload
) implements DomainEvent<UserActiveOrganizationChanged.Payload> {
    public record Payload(
        Long userId,
        Long organizationId
    ) {}
}
