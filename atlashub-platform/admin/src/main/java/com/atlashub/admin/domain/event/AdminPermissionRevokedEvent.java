package com.atlashub.admin.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminPermissionRevokedEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<AdminPermissionRevokedEvent.Payload> {
    
    public record Payload(String permission) {}

    public AdminPermissionRevokedEvent(Long adminId, String permission) {
        this(
            UUID.randomUUID().toString(), 
            String.valueOf(adminId), 
            ZonedDateTime.now(), 
            new Payload(permission)
        );
    }
}
