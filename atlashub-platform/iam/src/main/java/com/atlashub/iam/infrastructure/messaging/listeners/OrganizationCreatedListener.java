package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.InitializeOrganizationIam.InitializeOrganizationIamCommand;
import com.atlashub.iam.infrastructure.messaging.events.OrganizationRegistered;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.TimeoutException;

import com.atlashub.iam.application.commands.InitializeOrganizationIam.InitializeOrganizationIamHandler;
import org.springframework.stereotype.Component;

@Component("iamOrganizationCreatedListener")
public class OrganizationCreatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCreatedListener.class);
    private static final String GROUP_ID = "iam-group";
    
    private final InitializeOrganizationIamHandler handler;

    public OrganizationCreatedListener(ObjectMapper objectMapper, InitializeOrganizationIamHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(OrganizationRegistered.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "accounts-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationRegistered", OrganizationRegistered.class, log, GROUP_ID,
                e -> e instanceof TimeoutException, event -> {
                    InitializeOrganizationIamCommand command = new InitializeOrganizationIamCommand(event.aggregateId(), event.payload().ownerUserId());
                    handler.execute(command);
                });
    }
}
