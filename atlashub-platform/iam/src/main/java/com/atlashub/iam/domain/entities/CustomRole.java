package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.CustomRolePermissionsChangedEvent;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.iam.domain.exception.RoleModificationException;
import com.atlashub.iam.domain.exception.InvalidRolePermissionCountException;
import com.atlashub.iam.domain.exception.RoleInUseException;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class CustomRole extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private String description;
    private Set<Long> permissions;
    private final boolean builtIn;
    private final Long createdBy;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public CustomRole(Long id, Long organizationId, String name, String description, Set<Long> permissions, boolean builtIn, Long createdBy, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.builtIn = builtIn;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CustomRole create(Long id, Long organizationId, String name, String description, Set<Long> permissions, boolean builtIn, Long createdBy) {
        if (!builtIn && (permissions == null || permissions.isEmpty())) {
            throw new InvalidRolePermissionCountException("Minimum 1 permission per custom role");
        }

        ZonedDateTime now = ZonedDateTime.now();

        return new CustomRole(
                id, organizationId, name, description, permissions != null ? new HashSet<>(permissions) : new HashSet<>(), builtIn, createdBy, now, now
        );
    }

    public void addPermission(Long permissionId) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        permissions.add(permissionId);
        this.touch();
        this.publishPermissionsChangedEvent();
    }

    public void addPermissions(Set<Long> permissionIds) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        permissions.addAll(permissionIds);
        this.touch();
        this.publishPermissionsChangedEvent();
    }

    public void removePermission(Long permissionId) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        if (permissions.size() <= 1 && permissions.contains(permissionId)) {
            throw new InvalidRolePermissionCountException("Minimum 1 permission per custom role");
        }

        permissions.remove(permissionId);
        this.touch();
        this.publishPermissionsChangedEvent();
    }

    public void rename(String newName) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        this.name = newName;
        this.touch();
    }

    public void updateDescription(String newDescription) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        this.description = newDescription;
        this.touch();
    }

    public void updatePermissions(Set<Long> newPermissions) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot modify built-in OWNER role");
        }
        if (newPermissions == null || newPermissions.isEmpty()) {
            throw new InvalidRolePermissionCountException("Minimum 1 permission per custom role");
        }
        this.permissions = new HashSet<>(newPermissions);
        this.touch();
        this.publishPermissionsChangedEvent();
    }

    public void delete(boolean hasActiveMembers) {
        if (this.builtIn) {
            throw new RoleModificationException("Cannot delete built-in OWNER role");
        }
        if (hasActiveMembers) {
            throw new RoleInUseException("Cannot delete while it is assigned to an active member");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    private void publishPermissionsChangedEvent() {
        this.registerEvent(new CustomRolePermissionsChangedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new CustomRolePermissionsChangedEvent.Payload(this.organizationId)
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}


