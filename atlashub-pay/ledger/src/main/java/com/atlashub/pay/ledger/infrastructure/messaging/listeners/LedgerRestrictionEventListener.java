package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationBannedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationComplianceReinstatedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationComplianceSuspendedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationUnbannedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class LedgerRestrictionEventListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(LedgerRestrictionEventListener.class);
    private final ProcessLedgerEventHandler handler;

    public LedgerRestrictionEventListener(ObjectMapper objectMapper, ProcessLedgerEventHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationBannedEvent", "pay-ledger-org-banned");
        registerSubscription("OrganizationUnbannedEvent", "pay-ledger-org-unbanned");
        registerSubscription("OrganizationComplianceSuspendedEvent", "pay-ledger-compliance-suspended");
        registerSubscription("OrganizationComplianceReinstatedEvent", "pay-ledger-compliance-reinstated");
    }

    @KafkaListener(topics = "admin-events", groupId = "pay-ledger-org-banned")
    public void banned(String message) {
        consume(message, "OrganizationBannedEvent", OrganizationBannedEvent.class,
                "pay-ledger-org-banned", ProcessLedgerEventCommand.Action.RESTRICT_ORGANIZATION,
                "ORGANIZATION_BAN");
    }

    @KafkaListener(topics = "admin-events", groupId = "pay-ledger-org-unbanned")
    public void unbanned(String message) {
        consume(message, "OrganizationUnbannedEvent", OrganizationUnbannedEvent.class,
                "pay-ledger-org-unbanned", ProcessLedgerEventCommand.Action.RELEASE_ORGANIZATION,
                "ORGANIZATION_BAN");
    }

    @KafkaListener(topics = "compliance-events", groupId = "pay-ledger-compliance-suspended")
    public void complianceSuspended(String message) {
        consume(message, "OrganizationComplianceSuspendedEvent", OrganizationComplianceSuspendedEvent.class,
                "pay-ledger-compliance-suspended", ProcessLedgerEventCommand.Action.RESTRICT_ORGANIZATION,
                "COMPLIANCE");
    }

    @KafkaListener(topics = "compliance-events", groupId = "pay-ledger-compliance-reinstated")
    public void complianceReinstated(String message) {
        consume(message, "OrganizationComplianceReinstatedEvent", OrganizationComplianceReinstatedEvent.class,
                "pay-ledger-compliance-reinstated", ProcessLedgerEventCommand.Action.RELEASE_ORGANIZATION,
                "COMPLIANCE");
    }

    private <T> void consume(String message, String eventName, Class<T> eventClass, String groupId,
                             ProcessLedgerEventCommand.Action action, String restriction) {
        processEventIfMatches(message, eventName, eventClass, log, groupId,
                e -> e instanceof TimeoutException, event -> {
                    Long organizationId;
                    if (event instanceof OrganizationBannedEvent value) organizationId = value.payload().organizationId();
                    else if (event instanceof OrganizationUnbannedEvent value) organizationId = value.payload().organizationId();
                    else if (event instanceof OrganizationComplianceSuspendedEvent value) organizationId = value.payload().organizationId();
                    else organizationId = ((OrganizationComplianceReinstatedEvent) event).payload().organizationId();
                    handler.execute(new ProcessLedgerEventCommand(action, organizationId, null, null, restriction,
                            null, null, "SYSTEM", null, null, null));
                });
    }
}
