package com.atlashub.billing.infrastructure.messaging.listeners;

import com.atlashub.billing.application.command.ActivateSubscription.ActivateSubscriptionCommand;
import com.atlashub.billing.application.command.ActivateSubscription.ActivateSubscriptionHandler;
import com.atlashub.billing.infrastructure.messaging.events.OrganizationComplianceApprovedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@Component
public class OrganizationComplianceApprovedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationComplianceApprovedListener.class);
    private static final String GROUP_ID = "billing-compliance-approved-group";

    private final ActivateSubscriptionHandler handler;

    public OrganizationComplianceApprovedListener(ObjectMapper objectMapper, ActivateSubscriptionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationComplianceApprovedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationComplianceApprovedEvent", OrganizationComplianceApprovedKafkaEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            Long orgId = event.payload() != null ? event.payload().organizationId() : event.aggregateId();
            LocalDate now = LocalDate.now();
            handler.execute(new ActivateSubscriptionCommand(orgId, now, now.plusMonths(1)));
        });
    }
}
