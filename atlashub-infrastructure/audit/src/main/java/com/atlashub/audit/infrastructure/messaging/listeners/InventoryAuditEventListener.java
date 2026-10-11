package com.atlashub.audit.infrastructure.messaging.listeners;

import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogCommand;
import com.atlashub.audit.application.command.RecordAuditLog.RecordAuditLogHandler;
import com.atlashub.audit.domain.valueobject.AuditAction;
import com.atlashub.audit.domain.valueobject.AuditResourceType;
import com.atlashub.audit.infrastructure.messaging.events.AuditInboundEvents.StockAdjustedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class InventoryAuditEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryAuditEventListener.class);
    private static final String GROUP = "audit-inventory-group";

    private final RecordAuditLogHandler recordAuditLogHandler;

    public InventoryAuditEventListener(ObjectMapper objectMapper, RecordAuditLogHandler recordAuditLogHandler) {
        super(objectMapper);
        this.recordAuditLogHandler = recordAuditLogHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("StockAdjustedEvent", GROUP);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "StockAdjustedEvent", StockAdjustedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException,
                event -> recordAuditLogHandler.execute(new RecordAuditLogCommand(
                        event.adjustedByUserId(),
                        event.organizationId(),
                        AuditAction.STOCK_ADJUSTED.name(),
                        AuditResourceType.INVENTORY.name(),
                        String.valueOf(event.itemId()),
                        null,
                        "{\"quantityDelta\":" + event.quantityDelta() + ",\"reason\":\"" + event.reason() + "\"}",
                        null
                )));
    }
}
