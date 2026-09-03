package com.atlashub.identity.domain.event;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record InvitationAccepted(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    String invitedEmail,
    Long acceptingUserId
) implements DomainEvent<Void> {
    
    public InvitationAccepted(UUID invitationId, Long organizationId, String invitedEmail, Long acceptingUserId) {
        this(UUID.randomUUID().toString(), invitationId.toString(), ZonedDateTime.now(), organizationId, invitedEmail, acceptingUserId);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
