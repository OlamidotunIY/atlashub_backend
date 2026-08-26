package com.atlashub.identity.domain.event;

import com.atlashub.identity.domain.model.ComplianceStep;
import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record MerchantComplianceStepCompleted(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<MerchantComplianceStepCompleted.Payload> {
    public record Payload(ComplianceStep step) {}
}
