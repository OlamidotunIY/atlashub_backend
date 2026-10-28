package com.atlashub.billing.infrastructure.messaging.listeners;

import com.atlashub.billing.application.command.CancelSubscription.CancelSubscriptionCommand;
import com.atlashub.billing.application.command.CancelSubscription.CancelSubscriptionHandler;
import com.atlashub.billing.infrastructure.messaging.events.OrganizationBannedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component("billingOrganizationBannedListener")
public class OrganizationBannedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationBannedListener.class);
    private static final String GROUP_ID = "billing-organization-banned-group";

    private final CancelSubscriptionHandler handler;

    public OrganizationBannedListener(ObjectMapper objectMapper, CancelSubscriptionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationBannedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "admin-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationBannedEvent", OrganizationBannedKafkaEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            Long orgId = event.payload() != null ? event.payload().organizationId() : event.aggregateId();
            String reason = event.payload() != null ? event.payload().reason() : "Organization banned by admin";
            handler.execute(new CancelSubscriptionCommand(orgId, reason));
        });
    }
}
