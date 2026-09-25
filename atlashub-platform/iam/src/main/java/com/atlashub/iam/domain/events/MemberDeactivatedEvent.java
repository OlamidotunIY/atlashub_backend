package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.events.DomainEvent;
import java.time.ZonedDateTime;

public record MemberDeactivatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MemberDeactivatedEvent.Payload> {

    public record Payload(
        Long organizationId,
        Long userId
    ) {
    }
}
