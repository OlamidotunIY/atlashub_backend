package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationMemberAdded(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<OrganizationMemberAdded.Payload> {
    public record Payload(
        Long organizationId,
        Long userId,
        String role
    ) {}
}
