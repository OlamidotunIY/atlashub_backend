package com.atlashub.auth.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record AuthNewDeviceLoginEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        SessionPayload payload
) implements DomainEvent<SessionPayload> {}
