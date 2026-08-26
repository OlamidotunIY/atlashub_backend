package com.atlashub.auth.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record VerificationCreatedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        VerificationPayload payload
) implements DomainEvent<VerificationPayload> {}
