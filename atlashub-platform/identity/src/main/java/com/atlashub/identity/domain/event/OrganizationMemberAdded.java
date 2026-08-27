package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import java.time.ZonedDateTime;
import java.util.UUID;

public record OrganizationMemberAdded(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    Long userId,
    OrganizationRole role
) implements DomainEvent<Void> {
    
    public OrganizationMemberAdded(Long organizationId, Long userId, OrganizationRole role) {
        this(UUID.randomUUID().toString(), String.valueOf(organizationId), ZonedDateTime.now(), organizationId, userId, role);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
