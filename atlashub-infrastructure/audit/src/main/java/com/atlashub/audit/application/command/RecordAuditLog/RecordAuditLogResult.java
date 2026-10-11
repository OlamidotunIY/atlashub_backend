package com.atlashub.audit.application.command.RecordAuditLog;

import java.time.ZonedDateTime;

public record RecordAuditLogResult(
        Long id,
        Long userId,
        Long organizationId,
        String action,
        String resourceType,
        String resourceId,
        ZonedDateTime timestamp
) {}
