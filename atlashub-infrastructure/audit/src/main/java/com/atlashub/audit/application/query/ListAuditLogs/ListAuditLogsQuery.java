package com.atlashub.audit.application.query.ListAuditLogs;

public record ListAuditLogsQuery(
        Long organizationId,
        String resourceType,
        int page,
        int size
) {
    public ListAuditLogsQuery {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
    }
}
