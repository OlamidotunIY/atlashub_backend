package com.atlashub.pay.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationUnbannedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationUnbannedEvent.Payload> {
    public record Payload(Long organizationId, ZonedDateTime unbannedAt) {}
}
