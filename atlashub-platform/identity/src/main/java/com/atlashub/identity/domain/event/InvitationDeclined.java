package com.atlashub.identity.domain.event;

import com.atlashub.shared.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record InvitationDeclined(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Long organizationId,
    String invitedEmail
) implements DomainEvent<Void> {
    
    public InvitationDeclined(UUID invitationId, Long organizationId, String invitedEmail) {
        this(UUID.randomUUID().toString(), invitationId.toString(), ZonedDateTime.now(), organizationId, invitedEmail);
    }
    
    @Override
    public Void payload() {
        return null;
    }
}
