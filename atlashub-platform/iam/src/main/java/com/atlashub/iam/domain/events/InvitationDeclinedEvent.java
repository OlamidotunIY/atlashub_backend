package com.atlashub.iam.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record InvitationDeclinedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<InvitationDeclinedEvent.Payload> {

    public record Payload(
        Long organizationId,
        String invitedEmail
    ) {
    }
}
