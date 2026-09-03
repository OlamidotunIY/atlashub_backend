package com.atlashub.admin.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AdminCreatedEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<AdminCreatedEvent.Payload> {
    
    public record Payload(
        String username,
        String email,
        String role,
        String rawEmployeeCode,
        Long createdBy
    ) {}

    public AdminCreatedEvent(Long adminId, String username, String email, String role, String rawEmployeeCode, Long createdBy) {
        this(
            UUID.randomUUID().toString(), 
            String.valueOf(adminId), 
            ZonedDateTime.now(), 
            new Payload(username, email, role, rawEmployeeCode, createdBy)
        );
    }
}
