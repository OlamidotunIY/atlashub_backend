package com.atlashub.iam.application.queries.GetCustomRolePermissions;

import java.util.List;

public record CustomRolePermissionsResult(
    Long roleId,
    List<PermissionDetail> permissions
) {
    public record PermissionDetail(
        Long id,
        String code,
        String module,
        String resource,
        String action,
        String displayName,
        String description
    ) {}
}
