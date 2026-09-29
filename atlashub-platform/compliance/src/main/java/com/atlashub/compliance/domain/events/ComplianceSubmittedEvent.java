package com.atlashub.compliance.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ComplianceSubmittedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ComplianceSubmittedEvent.Payload> {

    public record Payload(
        Long organizationId,
        ZonedDateTime submittedAt
    ) {
    }
}
