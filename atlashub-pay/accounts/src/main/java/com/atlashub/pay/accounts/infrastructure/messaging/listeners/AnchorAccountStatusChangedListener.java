package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.anchor.infrastructure.messaging.events.AnchorAccountStatusChangedEvent;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusCommand;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusHandler;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class AnchorAccountStatusChangedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorAccountStatusChangedListener.class);
    private static final String GROUP_ID = "pay-accounts-anchor-status";
    private final ApplyAnchorAccountStatusHandler handler;

    public AnchorAccountStatusChangedListener(ObjectMapper objectMapper, ApplyAnchorAccountStatusHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(AnchorAccountStatusChangedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "anchor-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "AnchorAccountStatusChangedEvent", AnchorAccountStatusChangedEvent.class, log, GROUP_ID, error -> error instanceof TimeoutException, event -> {
            ConfirmedBankingDetails details = "FAILED".equalsIgnoreCase(event.status()) ? null : new ConfirmedBankingDetails(event.accountName(), event.accountNumber(), event.maskedAccountNumber(), event.bankName(), event.bankCode());
            handler.execute(new ApplyAnchorAccountStatusCommand(event.resourceType(), event.anchorResourceId(), event.status(), details, event.failureReason()));
        });
    }
}
