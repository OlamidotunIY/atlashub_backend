package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.InvitationAcceptedEvent;
import com.atlashub.iam.domain.events.InvitationCreatedEvent;
import com.atlashub.iam.domain.events.InvitationDeclinedEvent;
import com.atlashub.iam.domain.events.InvitationExpiredEvent;
import com.atlashub.iam.domain.events.InvitationRevokedEvent;
import com.atlashub.iam.domain.exception.InvalidInvitationStateException;
import com.atlashub.iam.domain.exception.InvitationExpiredException;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Invitation extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final EmailAddress invitedEmail;
    private final Long invitedByUserId;
    private final Long customRoleId;
    private final String token;
    private InvitationStatus status;
    private final ZonedDateTime expiresAt;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Invitation(Long id, Long organizationId, EmailAddress invitedEmail, Long invitedByUserId, Long customRoleId, String token, InvitationStatus status, ZonedDateTime expiresAt, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.invitedEmail = invitedEmail;
        this.invitedByUserId = invitedByUserId;
        this.customRoleId = customRoleId;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Invitation create(Long id, Long organizationId, EmailAddress invitedEmail, Long invitedByUserId, Long customRoleId, String token) {
        ZonedDateTime now = ZonedDateTime.now();
        Invitation invitation = new Invitation(
                id, organizationId, invitedEmail, invitedByUserId, customRoleId, token, InvitationStatus.PENDING, now.plusDays(7), now, now
        );

        invitation.registerEvent(new InvitationCreatedEvent(
                UUID.randomUUID().toString(),
                id,
                now,
                CorrelationId.getOrCreate(),
                new InvitationCreatedEvent.Payload(organizationId, invitedEmail.value(), token, invitedByUserId)
        ));

        return invitation;
    }

    public void accept(Long acceptingUserId) {
        if (this.isExpired()) {
            throw new InvitationExpiredException("Invitation has expired");
        }
        if (this.status != InvitationStatus.PENDING) {
            throw new InvalidInvitationStateException("Only pending invitations can be accepted");
        }
        
        this.status = InvitationStatus.ACCEPTED;
        this.touch();
        
        this.registerEvent(new InvitationAcceptedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new InvitationAcceptedEvent.Payload(this.organizationId, acceptingUserId, this.customRoleId)
        ));
    }

    public void decline() {
        if (this.status != InvitationStatus.PENDING) {
            throw new InvalidInvitationStateException("Only pending invitations can be declined");
        }
        this.status = InvitationStatus.DECLINED;
        this.touch();

        this.registerEvent(new InvitationDeclinedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new InvitationDeclinedEvent.Payload(this.organizationId, this.invitedEmail.value())
        ));
    }

    public void expire() {
        if (this.status != InvitationStatus.PENDING) {
            throw new InvalidInvitationStateException("Only pending invitations can be expired");
        }
        this.status = InvitationStatus.EXPIRED;
        this.touch();
        
        this.registerEvent(new InvitationExpiredEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new InvitationExpiredEvent.Payload(this.organizationId)
        ));
    }

    public void revoke(Long revokedByUserId) {
        if (this.status != InvitationStatus.PENDING) {
            throw new InvalidInvitationStateException("Only pending invitations can be revoked");
        }
        this.status = InvitationStatus.REVOKED;
        this.touch();

        this.registerEvent(new InvitationRevokedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new InvitationRevokedEvent.Payload(this.organizationId, revokedByUserId)
        ));
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    private boolean isExpired() {
        return ZonedDateTime.now().isAfter(this.expiresAt);
    }

    @Override
    public Long getId() {
        return id;
    }
}
