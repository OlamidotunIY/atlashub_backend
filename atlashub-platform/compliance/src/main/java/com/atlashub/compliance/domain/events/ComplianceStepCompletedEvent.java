package com.atlashub.compliance.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ComplianceStepCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ComplianceStepCompletedEvent.Payload> {

    public record Payload(
        Long organizationId,
        String stepName
    ) {
    }
}
