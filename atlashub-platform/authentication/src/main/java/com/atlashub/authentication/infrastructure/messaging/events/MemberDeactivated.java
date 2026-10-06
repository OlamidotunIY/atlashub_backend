package com.atlashub.authentication.infrastructure.messaging.events;

import com.atlashub.shared.domain.event.DomainEvent;

import java.time.ZonedDateTime;

public record MemberDeactivated(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MemberDeactivated.Payload> {
    public record Payload(Long userId, Long organizationId, ZonedDateTime deactivatedAt) {
    }
}
