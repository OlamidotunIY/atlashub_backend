package com.atlashub.identity.domain.model;

import com.atlashub.shared.domain.AggregateRoot;
import java.time.ZonedDateTime;

import com.atlashub.identity.domain.valueobject.OrganizationRole;
import com.atlashub.identity.domain.valueobject.MemberStatus;

public class OrganizationMember extends AggregateRoot<Long> {
    
    private Long id;
    private Long organizationId;
    private Long userId;
    private OrganizationRole role;
    private MemberStatus status;
    private ZonedDateTime joinedAt;

    protected OrganizationMember() {
        // JPA constructor
    }

    public static OrganizationMember reconstitute(Long id, Long organizationId, Long userId, OrganizationRole role, MemberStatus status, ZonedDateTime joinedAt) {
        OrganizationMember member = new OrganizationMember(organizationId, userId, role);
        member.id = id;
        member.status = status;
        member.joinedAt = joinedAt;
        return member;
    }

    public OrganizationMember(Long organizationId, Long userId, OrganizationRole role) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = ZonedDateTime.now();

        this.registerEvent(new com.atlashub.identity.domain.event.OrganizationMemberAdded(
            java.util.UUID.randomUUID().toString(),
            String.valueOf(this.organizationId),
            ZonedDateTime.now(),
            new com.atlashub.identity.domain.event.OrganizationMemberAdded.Payload(
                this.organizationId,
                this.userId,
                this.role.name()
            )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public OrganizationRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }
    
    public ZonedDateTime getJoinedAt() {
        return joinedAt;
    }
}
