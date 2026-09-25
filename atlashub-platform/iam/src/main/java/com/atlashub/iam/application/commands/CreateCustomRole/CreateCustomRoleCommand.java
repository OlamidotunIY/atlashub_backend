package com.atlashub.iam.application.commands.CreateCustomRole;

import java.util.Set;

public record CreateCustomRoleCommand(
    Long orgId,
    String name,
    String description,
    Set<Long> permissionIds
) {}
