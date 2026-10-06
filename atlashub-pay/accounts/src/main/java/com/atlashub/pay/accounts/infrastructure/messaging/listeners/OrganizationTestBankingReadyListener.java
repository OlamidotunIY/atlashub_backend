package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingCommand;
import com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking.ProvisionOrganizationBankingHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationTestBankingReadyEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationTestBankingReadyListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationTestBankingReadyListener.class);
    private static final String GROUP_ID = "pay-accounts-test-banking-ready";
    private final ProvisionOrganizationBankingHandler handler;

    public OrganizationTestBankingReadyListener(ObjectMapper objectMapper, ProvisionOrganizationBankingHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("com.atlashub.compliance.domain.events.OrganizationTestBankingReadyEvent", GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, OrganizationTestBankingReadyEvent.class.getSimpleName(),
                OrganizationTestBankingReadyEvent.class, log, GROUP_ID, error -> error instanceof TimeoutException,
                event -> handler.execute(new ProvisionOrganizationBankingCommand(event.payload().organizationId(),
                        event.payload().anchorBusinessCustomerId(), event.payload().environment())));
    }
}
