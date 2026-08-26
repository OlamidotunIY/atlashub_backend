package com.atlashub.admin.domain.model;

import com.atlashub.admin.domain.event.AdminPermissionGrantedEvent;
import com.atlashub.admin.domain.event.AdminPermissionRevokedEvent;
import com.atlashub.admin.domain.event.AdminSuspendedEvent;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Admin extends AggregateRoot<Long> {
    
    @Setter
    private Long id;
    @Getter
    private final String username;
    @Getter
    private final EmailAddress email;
    @Getter
    private final AdminRole role;
    @Getter
    private AdminStatus status;
    @Getter
    private final Long createdBy;
    private final Set<AdminPermission> permissions;
    
    public Admin(Long id, String username, EmailAddress email, AdminRole role, AdminStatus status, Long createdBy, Set<AdminPermission> permissions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdBy = createdBy;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }
    
    public static Admin create(Long id, String username, EmailAddress email, AdminRole role, Long createdBy, Set<AdminPermission> initialPermissions) {
        return new Admin(id, username, email, role, AdminStatus.ACTIVE, createdBy, initialPermissions);
    }
    
    public void suspend() {
        if (this.role == AdminRole.MASTER) {
            throw new IllegalStateException("MASTER admin cannot be suspended");
        }
        this.status = AdminStatus.SUSPENDED;
        this.registerEvent(new AdminSuspendedEvent(this.id, this.username));
    }
    
    public void activate() {
        this.status = AdminStatus.ACTIVE;
    }

    public void grantPermission(AdminPermission permission) {
        if (hasPermission(permission)) {
            throw new IllegalStateException("Admin already has permission: " + permission);
        }
        this.permissions.add(permission);
        this.registerEvent(new AdminPermissionGrantedEvent(this.id, permission.name()));
    }

    public void revokePermission(AdminPermission permission) {
        if (!this.permissions.contains(permission)) {
            throw new IllegalStateException("Admin does not hold permission: " + permission);
        }
        this.permissions.remove(permission);
        this.registerEvent(new AdminPermissionRevokedEvent(this.id, permission.name()));
    }

    public boolean hasPermission(AdminPermission permission) {
        if (this.role == AdminRole.MASTER) {
            return true;
        }
        return this.permissions.contains(permission);
    }

    @Override
    public Long getId() {
        return id;
    }

    public Set<AdminPermission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}

