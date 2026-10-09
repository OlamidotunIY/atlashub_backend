package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile.InitializeTestPaymentProfileCommand;
import com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile.InitializeTestPaymentProfileHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationRegistered;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationRegisteredPaymentProfileListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationRegisteredPaymentProfileListener.class);
    private static final String GROUP_ID = "pay-accounts-test-profile-registration";
    private final InitializeTestPaymentProfileHandler handler;

    public OrganizationRegisteredPaymentProfileListener(ObjectMapper objectMapper,
                                                        InitializeTestPaymentProfileHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("com.atlashub.accounts.domain.events.OrganizationRegistered", GROUP_ID);
    }

    @KafkaListener(topics = "accounts-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "OrganizationRegistered", OrganizationRegistered.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new InitializeTestPaymentProfileCommand(event.aggregateId())));
    }
}
