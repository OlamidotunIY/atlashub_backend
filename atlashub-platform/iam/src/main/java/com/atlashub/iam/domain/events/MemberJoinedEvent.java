package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.events.DomainEvent;
import java.time.ZonedDateTime;

public record MemberJoinedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MemberJoinedEvent.Payload> {

    public record Payload(
        Long organizationId,
        Long userId,
        Long customRoleId
    ) {
    }
}
