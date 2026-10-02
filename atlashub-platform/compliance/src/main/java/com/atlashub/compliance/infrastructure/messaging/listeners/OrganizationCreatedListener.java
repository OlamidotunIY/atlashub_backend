package com.atlashub.compliance.infrastructure.messaging.listeners;

import com.atlashub.compliance.application.commands.InitializeComplianceRecord.InitializeComplianceRecordCommand;
import com.atlashub.compliance.application.commands.InitializeComplianceRecord.InitializeComplianceRecordHandler;
import com.atlashub.compliance.infrastructure.messaging.events.OrganizationRegistered;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component("complianceOrganizationCreatedListener")
public class OrganizationCreatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCreatedListener.class);
    private static final String GROUP_ID = "compliance-org-created";
    private final InitializeComplianceRecordHandler handler;

    public OrganizationCreatedListener(ObjectMapper objectMapper, InitializeComplianceRecordHandler handler) {
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
                    InitializeComplianceRecordCommand command = new InitializeComplianceRecordCommand(event.aggregateId());
                    handler.execute(command);
                });
    }
}
