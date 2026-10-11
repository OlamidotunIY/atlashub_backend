package com.atlashub.audit.application.command.RecordAuditLog;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.domain.repositories.AuditLogRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class RecordAuditLogHandler extends Command<RecordAuditLogCommand, RecordAuditLogResult> {

    private final AuditLogRepository auditLogRepository;

    public RecordAuditLogHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public RecordAuditLogResult execute(RecordAuditLogCommand input) {
        Long id = auditLogRepository.nextIdentity();
        AuditLog auditLog = AuditLog.create(
                id,
                input.userId(),
                input.organizationId(),
                input.action(),
                input.resourceType(),
                input.resourceId(),
                input.previousState(),
                input.newState(),
                input.ipAddress(),
                ZonedDateTime.now()
        );

        AuditLog saved = auditLogRepository.save(auditLog);

        return new RecordAuditLogResult(
                saved.getId(),
                saved.getUserId(),
                saved.getOrganizationId(),
                saved.getAction(),
                saved.getResourceType(),
                saved.getResourceId(),
                saved.getTimestamp()
        );
    }
}
