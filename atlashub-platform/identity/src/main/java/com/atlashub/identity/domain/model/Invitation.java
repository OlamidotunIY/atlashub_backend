package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.valueobject.InvitationStatus;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import com.atlashub.shared.domain.AggregateRoot;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Invitation extends AggregateRoot<UUID> {
    
    private UUID id;
    private Long organizationId;
    private String invitedEmail;
    private Long invitedByUserId;
    private OrganizationRole role;
    private String token;
    private InvitationStatus status;
    private ZonedDateTime expiresAt;
    private ZonedDateTime createdAt;

    protected Invitation() {}

    public static Invitation reconstitute(UUID id, Long organizationId, String invitedEmail, Long invitedByUserId, OrganizationRole role, String token, InvitationStatus status, ZonedDateTime expiresAt, ZonedDateTime createdAt) {
        Invitation invitation = new Invitation();
        invitation.id = id;
        invitation.organizationId = organizationId;
        invitation.invitedEmail = invitedEmail;
        invitation.invitedByUserId = invitedByUserId;
        invitation.role = role;
        invitation.token = token;
        invitation.status = status;
        invitation.expiresAt = expiresAt;
        invitation.createdAt = createdAt;
        return invitation;
    }

    public Invitation(Long organizationId, String invitedEmail, Long invitedByUserId, OrganizationRole role, String token, ZonedDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.invitedEmail = invitedEmail;
        this.invitedByUserId = invitedByUserId;
        this.role = role;
        this.token = token;
        this.status = InvitationStatus.PENDING;
        this.expiresAt = expiresAt;
        this.createdAt = ZonedDateTime.now();
        registerEvent(new com.atlashub.identity.domain.event.InvitationCreated(this.id, this.organizationId, this.invitedEmail, this.role, this.token));
    }

    public void accept(Long acceptingUserId) {
        if (this.status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be accepted");
        }
        if (ZonedDateTime.now().isAfter(this.expiresAt)) {
            throw new IllegalStateException("Invitation has expired");
        }
        this.status = InvitationStatus.ACCEPTED;
        registerEvent(new com.atlashub.identity.domain.event.InvitationAccepted(this.id, this.organizationId, this.invitedEmail, acceptingUserId));
    }

    public void decline() {
        if (this.status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be declined");
        }
        this.status = InvitationStatus.DECLINED;
        registerEvent(new com.atlashub.identity.domain.event.InvitationDeclined(this.id, this.organizationId, this.invitedEmail));
    }

    public void expire() {
        if (this.status == InvitationStatus.PENDING) {
            this.status = InvitationStatus.EXPIRED;
            registerEvent(new com.atlashub.identity.domain.event.InvitationExpired(this.id, this.organizationId, this.invitedEmail));
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public OrganizationRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public ZonedDateTime getExpiresAt() {
        return expiresAt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
