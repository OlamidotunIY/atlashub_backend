package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.OrganizationBannedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.OrganizationComplianceApprovedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.OrganizationComplianceRejectedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.OrganizationComplianceSubmittedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ComplianceAuditEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ComplianceAuditEventListener.class);
    private static final String GROUP = "audit-compliance-group";

    private final RecordAuditLogHandler recordAuditLogHandler;

    public ComplianceAuditEventListener(ObjectMapper objectMapper, RecordAuditLogHandler recordAuditLogHandler) {
        super(objectMapper);
        this.recordAuditLogHandler = recordAuditLogHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationComplianceSubmittedEvent", GROUP);
        registerSubscription("OrganizationComplianceApprovedEvent", GROUP);
        registerSubscription("OrganizationComplianceRejectedEvent", GROUP);
        registerSubscription("OrganizationBannedEvent", GROUP);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "OrganizationComplianceSubmittedEvent", OrganizationComplianceSubmittedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.ORGANIZATION_COMPLIANCE_SUBMITTED.name(),
                        AuditResourceType.COMPLIANCE.name(),
                        String.valueOf(event.organizationId()),
                        null,
                        "{\"status\":\"SUBMITTED\"}",
                        null
                )));

        processEventIfMatches(message, "OrganizationComplianceApprovedEvent", OrganizationComplianceApprovedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.approvedByUserId(),
                        event.organizationId(),
                        AuditAction.ORGANIZATION_COMPLIANCE_APPROVED.name(),
                        AuditResourceType.COMPLIANCE.name(),
                        String.valueOf(event.organizationId()),
                        "{\"status\":\"SUBMITTED\"}",
                        "{\"status\":\"APPROVED\"}",
                        null
                )));

        processEventIfMatches(message, "OrganizationComplianceRejectedEvent", OrganizationComplianceRejectedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.rejectedByUserId(),
                        event.organizationId(),
                        AuditAction.ORGANIZATION_COMPLIANCE_REJECTED.name(),
                        AuditResourceType.COMPLIANCE.name(),
                        String.valueOf(event.organizationId()),
                        "{\"status\":\"SUBMITTED\"}",
                        "{\"status\":\"REJECTED\",\"reason\":\"" + event.reason() + "\"}",
                        null
                )));

        processEventIfMatches(message, "OrganizationBannedEvent", OrganizationBannedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.bannedByUserId(),
                        event.organizationId(),
                        AuditAction.ORGANIZATION_BANNED.name(),
                        AuditResourceType.ORGANIZATION.name(),
                        String.valueOf(event.organizationId()),
                        "{\"active\":true}",
                        "{\"active\":false,\"banned\":true,\"reason\":\"" + event.reason() + "\"}",
                        null
                )));
    }
}
