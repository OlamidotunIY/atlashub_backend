package com.atlashub.identity.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import java.time.ZonedDateTime;
import java.util.UUID;

public record InvitationCreated(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    String invitedEmail,
    OrganizationRole role,
    String token
) implements DomainEvent<Void> {
    
    public InvitationCreated(UUID invitationId, Long organizationId, String invitedEmail, OrganizationRole role, String token) {
        this(UUID.randomUUID().toString(), invitationId.toString(), ZonedDateTime.now(), organizationId, invitedEmail, role, token);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
