package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record InvitationExpiredEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvitationExpiredEvent.Payload> {

    public record Payload(
        Long organizationId
    ) {
    }
}
