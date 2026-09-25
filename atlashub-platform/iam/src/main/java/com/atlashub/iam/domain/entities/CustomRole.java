package com.atlashub.iam.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Set;

@Getter
public class CustomRole extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private String description;
    private Set<Long> permissions;
    private final boolean isBuiltIn;
    private final Long createdBy;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public CustomRole(Long id, Long organizationId, String name, String description, Set<Long> permissions, boolean isBuiltIn, Long createdBy, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.isBuiltIn = isBuiltIn;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomRole create(Long id, Long organizationId, String name, String description, boolean isBuiltIn, Long createdBy) {
        ZonedDateTime now = ZonedDateTime.now();

        return new CustomRole(
                id, organizationId, name, description, null, isBuiltIn, createdBy, now, now
        );
    }

    public void addPermission(Long permissionId) {
        permissions.add(permissionId);
        this.touch();
    }

    public void addPermissions(Set<Long> permissionIds) {
        permissions.addAll(permissionIds);
        this.touch();
    }

    public void removePermission(Long permissionId) {
        if(this.isBuiltIn) {
            return;
        }

        permissions.remove(permissionId);
        this.touch();
    }

    public void rename(String newName) {
        this.name = newName;

        this.touch();
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return 0L;
    }
}
