package com.atlashub.audit.presentation.dto;

import java.time.ZonedDateTime;

public record AuditLogResponse(
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
