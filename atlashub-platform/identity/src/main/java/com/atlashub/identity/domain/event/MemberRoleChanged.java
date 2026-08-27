package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import java.time.ZonedDateTime;
import java.util.UUID;

public record MemberRoleChanged(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    Long userId,
    OrganizationRole newRole
) implements DomainEvent<Void> {
    
    public MemberRoleChanged(Long organizationId, Long userId, OrganizationRole newRole) {
        this(UUID.randomUUID().toString(), String.valueOf(organizationId), ZonedDateTime.now(), organizationId, userId, newRole);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
