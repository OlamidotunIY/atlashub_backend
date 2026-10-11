package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.JournalEntryPostedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class FinanceAuditEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinanceAuditEventListener.class);
    private static final String GROUP = "audit-finance-group";

    private final RecordAuditLogHandler recordAuditLogHandler;

    public FinanceAuditEventListener(ObjectMapper objectMapper, RecordAuditLogHandler recordAuditLogHandler) {
        super(objectMapper);
        this.recordAuditLogHandler = recordAuditLogHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("JournalEntryPostedEvent", GROUP);
    }

    @KafkaListener(topics = "accounting-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "JournalEntryPostedEvent", JournalEntryPostedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.postedByUserId(),
                        event.organizationId(),
                        AuditAction.JOURNAL_ENTRY_POSTED.name(),
                        AuditResourceType.JOURNAL_ENTRY.name(),
                        String.valueOf(event.journalEntryId()),
                        "{\"status\":\"DRAFT\"}",
                        "{\"status\":\"POSTED\",\"entryNumber\":\"" + event.entryNumber() + "\"}",
                        null
                )));
    }
}
