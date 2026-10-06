package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionCommand;
import com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction.ApplyOrganizationBankingRestrictionHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationBannedEvent;
import com.atlashub.pay.accounts.infrastructure.messaging.events.OrganizationUnbannedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class OrganizationBankingRestrictionListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationBankingRestrictionListener.class);
    private static final String GROUP_ID = "pay-accounts-organization-restrictions";
    private final ApplyOrganizationBankingRestrictionHandler handler;

    public OrganizationBankingRestrictionListener(ObjectMapper objectMapper,
                                                  ApplyOrganizationBankingRestrictionHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationBannedEvent", GROUP_ID);
        registerSubscription("OrganizationUnbannedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "admin-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "OrganizationBannedEvent", OrganizationBannedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ApplyOrganizationBankingRestrictionCommand(
                        event.payload().organizationId(), true, "ORGANIZATION_BAN",
                        event.payload().reason(), event.eventId())));
        processEventIfMatches(message, "OrganizationUnbannedEvent", OrganizationUnbannedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new ApplyOrganizationBankingRestrictionCommand(
                        event.payload().organizationId(), false, "ORGANIZATION_BAN",
                        "Organization unbanned", event.eventId())));
    }
}
