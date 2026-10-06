package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionCommand;
import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationComplianceReinstatedEvent;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationComplianceSuspendedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationComplianceStatusListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationComplianceStatusListener.class);
    private static final String GROUP_ID = "pay-accounts-compliance-status";
    private final ApplyOrganizationBankingRestrictionHandler handler;

    public OrganizationComplianceStatusListener(ObjectMapper objectMapper,
                                                ApplyOrganizationBankingRestrictionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("com.atlashub.compliance.domain.events.OrganizationComplianceSuspendedEvent", GROUP_ID);
        registerSubscription("com.atlashub.compliance.domain.events.OrganizationComplianceReinstatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, OrganizationComplianceSuspendedEvent.class.getSimpleName(),
                OrganizationComplianceSuspendedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ApplyOrganizationBankingRestrictionCommand(
                        event.payload().organizationId(), true, "COMPLIANCE", event.payload().reason(), event.eventId())));
        processEventIfMatches(message, OrganizationComplianceReinstatedEvent.class.getSimpleName(),
                OrganizationComplianceReinstatedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ApplyOrganizationBankingRestrictionCommand(
                        event.payload().organizationId(), false, "COMPLIANCE", event.payload().reason(), event.eventId())));
    }
}
