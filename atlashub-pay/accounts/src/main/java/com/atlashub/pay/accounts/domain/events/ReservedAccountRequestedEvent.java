package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ReservedAccountRequestedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ReservedAccountRequestedEvent.Payload> {
    public record Payload(Long organizationId, String environment, String ownerType,
                          String ownerReferenceId, String requestReference, ZonedDateTime requestedAt) {}
}
