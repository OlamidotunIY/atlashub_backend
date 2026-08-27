package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record OrganizationMemberRemoved(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    Long userId
) implements DomainEvent<Void> {
    
    public OrganizationMemberRemoved(Long organizationId, Long userId) {
        this(UUID.randomUUID().toString(), String.valueOf(organizationId), ZonedDateTime.now(), organizationId, userId);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
