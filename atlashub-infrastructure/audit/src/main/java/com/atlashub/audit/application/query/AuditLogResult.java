package com.atlashub.audit.application.query;

import java.time.ZonedDateTime;

public record AuditLogResult(
        Long id,
        Long userId,
        Long organizationId,
        String action,
        String resourceType,
        String resourceId,
        String previousState,
        String newState,
        String ipAddress,
        ZonedDateTime timestamp
) {}
