package com.atlashub.pay.accounts.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record VirtualAccountSuspendedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Void payload
) implements DomainEvent<Void> {
}