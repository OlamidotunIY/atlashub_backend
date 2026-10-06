package com.atlashub.pay.accounts.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record OrganizationBannedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<OrganizationBannedEvent.Payload> {
    public record Payload(Long organizationId, String reason, ZonedDateTime bannedAt) {}
}
