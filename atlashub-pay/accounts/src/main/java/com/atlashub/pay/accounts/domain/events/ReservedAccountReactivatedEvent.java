package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ReservedAccountReactivatedEvent(
        String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId, Payload payload)
        implements DomainEvent<ReservedAccountReactivatedEvent.Payload> {
    public record Payload(Long organizationId, String environment, ZonedDateTime reactivatedAt) {}
}
