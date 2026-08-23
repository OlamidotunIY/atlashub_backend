package com.atlaspay.admin.domain.event;

import com.atlaspay.shared.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminSuspendedEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<AdminSuspendedEvent.Payload> {
    
    public record Payload(String username) {}

    public AdminSuspendedEvent(Long adminId, String username) {
        this(
            UUID.randomUUID().toString(), 
            String.valueOf(adminId), 
            ZonedDateTime.now(), 
            new Payload(username)
        );
    }
}
