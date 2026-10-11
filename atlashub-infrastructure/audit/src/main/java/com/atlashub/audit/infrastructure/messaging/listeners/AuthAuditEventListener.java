package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.AuthAccountLockedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.AuthPasswordChangedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.AuthSessionCreatedEvent;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.AuthSessionRevokedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class AuthAuditEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthAuditEventListener.class);
    private static final String GROUP = "audit-auth-group";

    private final RecordAuditLogHandler recordAuditLogHandler;

    public AuthAuditEventListener(ObjectMapper objectMapper, RecordAuditLogHandler recordAuditLogHandler) {
        super(objectMapper);
        this.recordAuditLogHandler = recordAuditLogHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("AuthSessionCreatedEvent", GROUP);
        registerSubscription("AuthSessionRevokedEvent", GROUP);
        registerSubscription("AuthAccountLockedEvent", GROUP);
        registerSubscription("AuthPasswordChangedEvent", GROUP);
    }

    @KafkaListener(topics = "auth-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "AuthSessionCreatedEvent", AuthSessionCreatedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.AUTH_SESSION_CREATED.name(),
                        AuditResourceType.SESSION.name(),
                        String.valueOf(event.aggregateId()),
                        null,
                        "{\"status\":\"ACTIVE\"}",
                        event.ipAddress()
                )));

        processEventIfMatches(message, "AuthSessionRevokedEvent", AuthSessionRevokedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.AUTH_SESSION_REVOKED.name(),
                        AuditResourceType.SESSION.name(),
                        String.valueOf(event.aggregateId()),
                        "{\"status\":\"ACTIVE\"}",
                        "{\"status\":\"REVOKED\",\"reason\":\"" + event.reason() + "\"}",
                        null
                )));

        processEventIfMatches(message, "AuthAccountLockedEvent", AuthAccountLockedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.AUTH_ACCOUNT_LOCKED.name(),
                        AuditResourceType.ACCOUNT.name(),
                        String.valueOf(event.aggregateId()),
                        "{\"locked\":false}",
                        "{\"locked\":true,\"reason\":\"" + event.reason() + "\"}",
                        null
                )));

        processEventIfMatches(message, "AuthPasswordChangedEvent", AuthPasswordChangedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.userId(),
                        event.organizationId(),
                        AuditAction.AUTH_PASSWORD_CHANGED.name(),
                        AuditResourceType.ACCOUNT.name(),
                        String.valueOf(event.aggregateId()),
                        null,
                        "{\"passwordUpdated\":true}",
                        null
                )));
    }
}
