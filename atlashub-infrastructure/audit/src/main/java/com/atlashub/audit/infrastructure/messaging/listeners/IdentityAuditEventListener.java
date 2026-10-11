package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.InvitationAcceptedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.OrganizationMemberAddedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class IdentityAuditEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(IdentityAuditEventListener.class);
    private static final String GROUP = "audit-identity-group";

    private final RecordAuditLogHandler recordAuditLogHandler;

    public IdentityAuditEventListener(ObjectMapper objectMapper, RecordAuditLogHandler recordAuditLogHandler) {
        super(objectMapper);
        this.recordAuditLogHandler = recordAuditLogHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("InvitationAcceptedEvent", GROUP);
        registerSubscription("OrganizationMemberAddedEvent", GROUP);
    }

    @KafkaListener(topics = "identity-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "InvitationAcceptedEvent", InvitationAcceptedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.INVITATION_ACCEPTED.name(),
                        AuditResourceType.INVITATION.name(),
                        String.valueOf(event.invitationId()),
                        "{\"status\":\"PENDING\"}",
                        "{\"status\":\"ACCEPTED\"}",
                        null
                )));

        processEventIfMatches(message, "OrganizationMemberAddedEvent", OrganizationMemberAddedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.memberUserId(),
                        event.organizationId(),
                        AuditAction.ORGANIZATION_MEMBER_ADDED.name(),
                        AuditResourceType.MEMBER.name(),
                        String.valueOf(event.memberUserId()),
                        null,
                        "{\"role\":\"" + event.role() + "\"}",
                        null
                )));
    }
}
