package com.atlashub.iam.application.commands.UpdateCustomRole;

import java.util.Set;

public record UpdateCustomRoleCommand(
    Long roleId,
    String name,
    String description,
    Set<Long> permissionIds
) {}
