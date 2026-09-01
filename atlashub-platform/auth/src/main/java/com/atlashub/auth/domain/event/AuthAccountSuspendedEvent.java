package com.atlashub.auth.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;

public record AuthAccountSuspendedEvent(
        String eventId,
        String aggregateId,
        ZonedDateTime occurredAt,
        Void payload
) implements DomainEvent<Void> {}
