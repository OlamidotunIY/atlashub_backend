package com.atlashub.audit.application.query.GetAuditLogById;

import com.atlashub.audit.application.query.AuditLogResult;
import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.exceptions.AuditLogNotFoundException;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetAuditLogByIdHandler extends Query<GetAuditLogByIdQuery, AuditLogResult> {

    private final AuditLogRepository auditLogRepository;

    public GetAuditLogByIdHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public AuditLogResult execute(GetAuditLogByIdQuery query) {
        AuditLog auditLog = auditLogRepository.findById(query.id())
                .orElseThrow(() -> new AuditLogNotFoundException(query.id()));

        if (query.organizationId() != null && !Objects.equals(auditLog.getOrganizationId(), query.organizationId())) {
            throw new AuditLogNotFoundException(query.id());
        }

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
