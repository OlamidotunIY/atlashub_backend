package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationUpdated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<OrganizationUpdated.Payload> {
    public record Payload(
        String businessName,
        String description,
        String logoUrl
    ) {}
}
