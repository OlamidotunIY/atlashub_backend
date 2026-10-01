package com.atlashub.accounts.infrastructure.messaging.listeners;

import com.atlashub.accounts.application.command.SuspendOrganizationSubscription.SuspendOrganizationSubscriptionCommand;
import com.atlashub.accounts.application.command.SuspendOrganizationSubscription.SuspendOrganizationSubscriptionHandler;
import com.atlashub.accounts.infrastructure.messaging.events.SubscriptionSuspended;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class SubscriptionSuspendedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionSuspendedListener.class);
    private static final String GROUP_ID = "accounts-billing-group";

    private final SuspendOrganizationSubscriptionHandler handler;

    public SubscriptionSuspendedListener(ObjectMapper objectMapper, SuspendOrganizationSubscriptionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("SubscriptionSuspended", GROUP_ID);
    }

    @KafkaListener(topics = "billing-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "SubscriptionSuspended", SubscriptionSuspended.class, log, GROUP_ID,
                e -> e instanceof TimeoutException, event -> {
                    handler.execute(
                            new SuspendOrganizationSubscriptionCommand(
                                    event.payload().organizationId(),
                                    event.payload().reason()
                            ));
                });
    }
}
