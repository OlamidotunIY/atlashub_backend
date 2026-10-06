package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.TillClosedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.TillOpenedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class CommerceLedgerEventListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(CommerceLedgerEventListener.class);
    private final ProcessLedgerEventHandler handler;

    public CommerceLedgerEventListener(ObjectMapper objectMapper, ProcessLedgerEventHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("TillOpenedEvent", "pay-ledger-till-opened");
        registerSubscription("TillClosedEvent", "pay-ledger-till-closed");
    }

    @KafkaListener(topics = "commerce-events", groupId = "pay-ledger-till-opened")
    public void tillOpened(String message) {
        processEventIfMatches(message, "TillOpenedEvent", TillOpenedEvent.class, log,
                "pay-ledger-till-opened", e -> e instanceof TimeoutException, event ->
                        handler.execute(new ProcessLedgerEventCommand(
                                ProcessLedgerEventCommand.Action.TILL_OPENED, event.organizationId(),
                                event.environment(), event.outletId(), null, null, event.tillSessionId() + "-open",
                                "INTER_OUTLET_TRANSFER", event.tillSessionId(), event.openingFloat(),
                                event.currency())));
    }

    @KafkaListener(topics = "commerce-events", groupId = "pay-ledger-till-closed")
    public void tillClosed(String message) {
        processEventIfMatches(message, "TillClosedEvent", TillClosedEvent.class, log,
                "pay-ledger-till-closed", e -> e instanceof TimeoutException, event ->
                        handler.execute(new ProcessLedgerEventCommand(
                                ProcessLedgerEventCommand.Action.TILL_CLOSED, event.organizationId(),
                                event.environment(), event.outletId(), null, null, event.tillSessionId() + "-close",
                                "CASH_BANKING", event.tillSessionId(), event.closingCash(),
                                event.currency())));
    }
}
