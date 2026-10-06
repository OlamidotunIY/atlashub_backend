package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.InvitationAcceptedEvent;
import com.atlashub.iam.domain.exception.InvitationNotExpiredException;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvitationTest {
    @Test
    void accepts_pending_invitation_and_emits_event() {
        Invitation invitation = Invitation.create(1L, 2L, new EmailAddress("member@example.com"),
                3L, 4L, "hash", "plain-token");
        invitation.pullDomainEvents();
        invitation.accept(5L);
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertInstanceOf(InvitationAcceptedEvent.class, invitation.pullDomainEvents().getFirst());
    }

    @Test
    void cannot_expire_before_deadline() {
        Invitation invitation = Invitation.create(1L, 2L, new EmailAddress("member@example.com"),
                3L, 4L, "hash", "plain-token");
        assertThrows(InvitationNotExpiredException.class, invitation::expire);
    }
}
