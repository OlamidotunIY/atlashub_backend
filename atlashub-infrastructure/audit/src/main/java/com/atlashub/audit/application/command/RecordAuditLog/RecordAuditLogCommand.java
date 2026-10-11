package com.atlashub.audit.application.command.RecordAuditLog;

public record RecordAuditLogCommand(
        Long userId,
        Long organizationId,
        String action,
        String resourceType,
        String resourceId,
        String previousState,
        String newState,
        String ipAddress
) {}
