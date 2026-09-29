package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.MemberDeactivatedEvent;
import com.atlashub.iam.domain.events.MemberJoinedEvent;
import com.atlashub.iam.domain.exception.LastOwnerDeactivationException;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class OrganizationMember extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long userId;
    private Long customRoleId;
    private MemberStatus status;
    private final ZonedDateTime joinedAt;
    private final Long invitedBy;
    private ZonedDateTime updatedAt;

    public OrganizationMember(Long id, Long organizationId, Long userId, Long customRoleId, MemberStatus status, ZonedDateTime joinedAt, Long invitedBy, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.customRoleId = customRoleId;
        this.status = status;
        this.joinedAt = joinedAt;
        this.invitedBy = invitedBy;
        this.updatedAt = updatedAt;
    }

    public static OrganizationMember create(Long id, Long organizationId, Long userId, Long customRoleId, Long invitedBy) {
        ZonedDateTime now = ZonedDateTime.now();

        OrganizationMember member = new OrganizationMember(id, organizationId, userId, customRoleId, MemberStatus.ACTIVE, now, invitedBy, now);
        member.registerEvent(new MemberJoinedEvent(
                UUID.randomUUID().toString(),
                id,
                now,
                CorrelationId.getOrCreate(),
                new MemberJoinedEvent.Payload(organizationId, userId, customRoleId)
        ));
        return member;
    }

    public void assignRole(Long newRoleId) {
        if (!newRoleId.equals(this.customRoleId)) {
            this.customRoleId = newRoleId;
            this.touch();
        }
    }

    public void deactivate(boolean isLastOwner) {
        if (isLastOwner) {
            throw new LastOwnerDeactivationException("Cannot deactivate the last OWNER of the organization.");
        }
        if (this.status.equals(MemberStatus.ACTIVE)) {
            this.status = MemberStatus.INACTIVE;
            this.touch();
            this.registerEvent(new MemberDeactivatedEvent(
                    UUID.randomUUID().toString(),
                    this.id,
                    this.updatedAt,
                    CorrelationId.getOrCreate(),
                    new MemberDeactivatedEvent.Payload(this.organizationId, this.userId)
            ));
        }
    }

    public void suspend(String reason) {
        if (this.status.equals(MemberStatus.ACTIVE)) {
            this.status = MemberStatus.SUSPENDED;
            this.touch();
        }
    }

    public void reactivate() {
        if (this.status.equals(MemberStatus.INACTIVE) || this.status.equals(MemberStatus.SUSPENDED)) {
            this.status = MemberStatus.ACTIVE;
            this.touch();
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
