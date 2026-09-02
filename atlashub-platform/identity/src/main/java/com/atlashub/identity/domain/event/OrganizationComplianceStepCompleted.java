package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.valueobject.ComplianceStep;
import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationComplianceStepCompleted(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<OrganizationComplianceStepCompleted.Payload> {
    public record Payload(ComplianceStep step) {}
}

