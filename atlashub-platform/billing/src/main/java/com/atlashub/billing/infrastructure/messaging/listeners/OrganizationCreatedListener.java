package com.atlashub.billing.infrastructure.messaging.listeners;

import com.atlashub.billing.application.command.InitialiseSubscription.InitialiseSubscriptionCommand;
import com.atlashub.billing.application.command.InitialiseSubscription.InitialiseSubscriptionHandler;
import com.atlashub.billing.infrastructure.messaging.events.OrganizationRegisteredKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationCreatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCreatedListener.class);
    private static final String GROUP_ID = "billing-organization-created-group";

    private final InitialiseSubscriptionHandler handler;

    public OrganizationCreatedListener(ObjectMapper objectMapper, InitialiseSubscriptionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationRegistered", GROUP_ID);
        registerSubscription("OrganizationCreatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "accounts-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationRegistered", OrganizationRegisteredKafkaEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            Long orgId = event.aggregateId();
            String currency = event.payload() != null ? event.payload().currency() : "NGN";
            handler.execute(new InitialiseSubscriptionCommand(orgId, 1L, currency));
        });
        processEventIfMatches(messagePayload, "OrganizationCreatedEvent", OrganizationRegisteredKafkaEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            Long orgId = event.aggregateId();
            String currency = event.payload() != null ? event.payload().currency() : "NGN";
            handler.execute(new InitialiseSubscriptionCommand(orgId, 1L, currency));
        });
    }
}
