package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

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

        return new OrganizationMember(id, organizationId, userId, customRoleId, MemberStatus.ACTIVE, now, invitedBy, now);
    }

    public void assignRole(Long newRoleId) {
        this.customRoleId = newRoleId;
        this.touch();
    }

    public void deactivate() {
        if (this.status.equals(MemberStatus.ACTIVE)) {
            this.status = MemberStatus.INACTIVE;
            this.touch();
        }

        return;
    }

    public void suspend(String reason) {
        if (this.status.equals(MemberStatus.ACTIVE)) {
            this.status = MemberStatus.SUSPENDED;
            this.touch();
        }

        return;
    }

    public void reactivate() {
        if (this.status.equals(MemberStatus.INACTIVE)) {
            this.status = MemberStatus.ACTIVE;
            this.touch();
        }

        return;
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
