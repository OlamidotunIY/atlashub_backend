package com.atlashub.iam.application.queries.ListCustomRoles;

import java.time.ZonedDateTime;
import java.util.Set;

public record CustomRoleResult(
    Long id,
    Long organizationId,
    String name,
    String description,
    Set<Long> permissions,
    boolean isBuiltIn,
    Long createdBy,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
