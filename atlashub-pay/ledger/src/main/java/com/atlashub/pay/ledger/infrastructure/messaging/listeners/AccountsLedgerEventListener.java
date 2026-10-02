package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OutletCreatedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class AccountsLedgerEventListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AccountsLedgerEventListener.class);
    private static final String GROUP_ID = "pay-ledger-outlet-created";
    private final ProcessLedgerEventHandler handler;

    public AccountsLedgerEventListener(ObjectMapper objectMapper, ProcessLedgerEventHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(OutletCreatedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "accounts-events", groupId = GROUP_ID)
    public void outletCreated(String message) {
        processEventIfMatches(message, "OutletCreatedEvent", OutletCreatedEvent.class, log, GROUP_ID,
                e -> e instanceof TimeoutException, event -> handler.execute(new ProcessLedgerEventCommand(
                        ProcessLedgerEventCommand.Action.CREATE_TILL_ACCOUNT, event.organizationId(),
                        event.outletId(), null, null, null, "SYSTEM", event.outletId().toString(),
                        null, event.currency())));
    }
}
