package com.atlashub.compliance.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationComplianceRejectedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<OrganizationComplianceRejectedEvent.Payload> {

    public record Payload(
        Long organizationId,
        Long rejectedBy,
        String reason,
        ZonedDateTime rejectedAt
    ) {
    }
}
