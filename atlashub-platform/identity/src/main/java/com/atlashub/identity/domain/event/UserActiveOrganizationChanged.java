package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record UserActiveOrganizationChanged(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<UserActiveOrganizationChanged.Payload> {
    public record Payload(
        Long userId,
        Long organizationId
    ) {}
}
