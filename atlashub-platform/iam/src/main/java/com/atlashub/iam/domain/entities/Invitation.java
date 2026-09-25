package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import lombok.Getter;

import java.time.ZonedDateTime;

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

    public Invitation(Long id, Long organizationId, EmailAddress invitedEmail, Long invitedByUserId, Long customRoleId, String token, InvitationStatus status, ZonedDateTime expiresAt, ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.invitedEmail = invitedEmail;
        this.invitedByUserId = invitedByUserId;
        this.customRoleId = customRoleId;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static Invitation create(Long id, Long organizationId, EmailAddress invitedEmail, Long invitedByUserId, Long customRoleId, String token) {
        ZonedDateTime now = ZonedDateTime.now();

        return new Invitation(
                id, organizationId, invitedEmail, invitedByUserId, customRoleId, token, InvitationStatus.PENDING, expiresAt(), now
        );
    }

    public void accept(Long acceptingUserId) {
        if (this.isExpired()) {
            this.status = InvitationStatus.ACCEPTED;

            // throw event
        }

        // throw error invitation expired
    }

    public void decline() {
        if (this.status.equals(InvitationStatus.PENDING)) {
            this.status = InvitationStatus.ACCEPTED;
        }
    }

    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }

    public void revoke(Long revokedByUserId) {
        this.status = InvitationStatus.REVOKED;
    }

    private static ZonedDateTime expiresAt() {
        return ZonedDateTime.now().plusHours(24);
    }

    private Boolean isExpired() {
        return this.expiresAt.isAfter(ZonedDateTime.now());
    }

    @Override
    public Long getId() {
        return id;
    }
}
