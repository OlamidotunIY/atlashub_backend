package com.atlashub.identity.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationComplianceApproved(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<OrganizationComplianceApproved.Payload> {
    public record Payload(String businessName) {}
}
