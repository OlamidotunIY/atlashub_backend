package com.atlashub.audit.application.query.ListAuditLogs;

import com.atlashub.audit.application.query.AuditLogResult;
import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListAuditLogsHandler extends Query<ListAuditLogsQuery, List<AuditLogResult>> {

    private final AuditLogRepository auditLogRepository;

    public ListAuditLogsHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<AuditLogResult> execute(ListAuditLogsQuery query) {
        List<AuditLog> logs;
        if (query.resourceType() != null && !query.resourceType().isBlank()) {
            logs = auditLogRepository.findByOrganizationIdAndResourceType(
                    query.organizationId(),
                    query.resourceType().trim(),
                    query.page(),
                    query.size()
            );
        } else {
            logs = auditLogRepository.findByOrganizationId(
                    query.organizationId(),
                    query.page(),
                    query.size()
            );
        }

        return logs.stream()
                .map(this::toResult)
                .toList();
    }

    private AuditLogResult toResult(AuditLog auditLog) {
        return new AuditLogResult(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getOrganizationId(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getPreviousState(),
                auditLog.getNewState(),
                auditLog.getIpAddress(),
                auditLog.getTimestamp()
        );
    }
}
