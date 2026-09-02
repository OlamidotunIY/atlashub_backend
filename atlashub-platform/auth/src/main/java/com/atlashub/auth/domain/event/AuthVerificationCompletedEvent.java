package com.atlashub.auth.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record AuthVerificationCompletedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        VerificationPayload payload
) implements DomainEvent<VerificationPayload> {}
