package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingCommand;
import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationComplianceApprovedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationComplianceApprovedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationComplianceApprovedListener.class);
    private static final String GROUP_ID = "pay-accounts-compliance-approved";
    private final ProvisionOrganizationBankingHandler handler;

    public OrganizationComplianceApprovedListener(ObjectMapper objectMapper, ProvisionOrganizationBankingHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(OrganizationComplianceApprovedEvent.class.getName(), GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "OrganizationComplianceApprovedEvent", OrganizationComplianceApprovedEvent.class, log, GROUP_ID, error -> error instanceof TimeoutException, event -> handler.execute(new ProvisionOrganizationBankingCommand(
                event.payload().organizationId(),
                event.payload().anchorBusinessCustomerId(),
                event.payload().environment())));
    }
}
