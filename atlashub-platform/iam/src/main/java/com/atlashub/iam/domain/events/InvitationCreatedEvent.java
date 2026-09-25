package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.events.DomainEvent;
import java.time.ZonedDateTime;

public record InvitationCreatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvitationCreatedEvent.Payload> {

    public record Payload(
        Long organizationId,
        String invitedEmail,
        String token,
        Long invitedByUserId
    ) {
    }
}
