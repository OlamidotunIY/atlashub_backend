package com.atlashub.pay.controls.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ControlCaseResolvedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ControlCaseResolvedEvent.Payload> {

    public record Payload(
            Long caseId,
            Long decisionId,
            Long organizationId,
            String status,
            Long resolvedBy,
            String resolutionReason,
            ZonedDateTime resolvedAt
    ) {}
}
