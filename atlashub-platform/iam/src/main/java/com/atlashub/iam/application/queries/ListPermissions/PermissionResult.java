package com.atlashub.iam.application.queries.ListPermissions;

import com.atlashub.iam.domain.entities.Permission;

public record PermissionResult(
    Long id,
    String code,
    String module,
    String resource,
    String action,
    String displayName,
    String description,
    boolean active
) {
    public static PermissionResult fromEntity(Permission permission) {
        return new PermissionResult(
            permission.getId(),
            permission.getCode(),
            permission.getModule(),
            permission.getResource(),
            permission.getAction() != null ? permission.getAction().name() : null,
            permission.getDisplayName(),
            permission.getDescription(),
            permission.isActive()
        );
    }
}

