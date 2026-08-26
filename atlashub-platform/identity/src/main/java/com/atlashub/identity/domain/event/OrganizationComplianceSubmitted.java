package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;

import java.time.ZonedDateTime;

public record OrganizationComplianceSubmitted(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt
) implements DomainEvent<Void> {
    @Override
    public Void payload() {
        return null;
    }
}
