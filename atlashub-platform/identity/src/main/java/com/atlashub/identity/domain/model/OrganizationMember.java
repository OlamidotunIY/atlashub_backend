package com.atlashub.identity.domain.model;

import com.atlashub.shared.domain.AggregateRoot;
import java.time.ZonedDateTime;

public class OrganizationMember extends AggregateRoot<Long> {
    
    private Long id;
    private String organizationId;
    private String userId;
    private OrganizationRole role;
    private MemberStatus status;
    private ZonedDateTime joinedAt;

    public enum OrganizationRole {
        OWNER, ADMIN, MANAGER, VIEWER
    }

    public enum MemberStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    protected OrganizationMember() {
        // JPA constructor
    }

    public OrganizationMember(String organizationId, String userId, OrganizationRole role) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getUserId() {
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
