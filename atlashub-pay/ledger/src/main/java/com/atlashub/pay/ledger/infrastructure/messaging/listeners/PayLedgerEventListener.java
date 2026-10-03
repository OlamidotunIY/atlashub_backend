package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ChargeSuccessfulEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationAccountFundedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationBankingActivatedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.PayoutCompletedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ProviderSettlementReceivedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ReservedAccountActivatedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ReservedAccountFundedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;
import java.math.BigDecimal;

@Component
public class PayLedgerEventListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(PayLedgerEventListener.class);
    private final ProcessLedgerEventHandler handler;

    public PayLedgerEventListener(ObjectMapper objectMapper, ProcessLedgerEventHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(OrganizationBankingActivatedEvent.class.getName(), "pay-ledger-org-banking-activated");
        registerSubscription(ReservedAccountActivatedEvent.class.getName(), "pay-ledger-reserved-account-activated");
        registerSubscription(ChargeSuccessfulEvent.class.getName(), "pay-ledger-charge-successful");
        registerSubscription(ReservedAccountFundedEvent.class.getName(), "pay-ledger-reserved-account-funded");
        registerSubscription(OrganizationAccountFundedEvent.class.getName(), "pay-ledger-organization-account-funded");
        registerSubscription(PayoutCompletedEvent.class.getName(), "pay-ledger-payout-completed");
        registerSubscription(ProviderSettlementReceivedEvent.class.getName(), "pay-ledger-provider-settled");
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-org-banking-activated")
    public void bankingActivated(String message) {
        processEventIfMatches(message, "OrganizationBankingActivatedEvent", OrganizationBankingActivatedEvent.class,
                log, "pay-ledger-org-banking-activated", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.BOOTSTRAP_ORGANIZATION,
                                event.payload().organizationId(), event.payload().environment(), null, null, null,
                                null, "SYSTEM", null, null, event.payload().currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-reserved-account-activated")
    public void reservedActivated(String message) {
        processEventIfMatches(message, "ReservedAccountActivatedEvent", ReservedAccountActivatedEvent.class,
                log, "pay-ledger-reserved-account-activated", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.CREATE_PARTY_ACCOUNT,
                                event.payload().organizationId(), event.payload().environment(), null,
                                event.payload().ownerType(), event.payload().ownerReferenceId(), null,
                                "SYSTEM", event.payload().reservedAccountId().toString(), null,
                                event.payload().currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-charge-successful")
    public void chargeSuccessful(String message) {
        processEventIfMatches(message, "ChargeSuccessfulEvent", ChargeSuccessfulEvent.class,
                log, "pay-ledger-charge-successful", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.CHARGE_RECEIVED,
                                event.organizationId(), event.environment(), null, null, null, event.chargeReference(),
                                "CARD_CHARGE", event.sourceReferenceId(), event.amount(), event.currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-reserved-account-funded")
    public void reservedFunded(String message) {
        processEventIfMatches(message, "ReservedAccountFundedEvent", ReservedAccountFundedEvent.class,
                log, "pay-ledger-reserved-account-funded", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.RESERVED_ACCOUNT_FUNDED,
                                event.organizationId(), event.environment(), null, event.ownerType(), event.ownerReferenceId(),
                                event.anchorTransferReference(), "EXTERNAL_COLLECTION",
                                event.reservedAccountId().toString(), event.amount(), event.currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-organization-account-funded")
    public void organizationFunded(String message) {
        processEventIfMatches(message, "OrganizationAccountFundedEvent", OrganizationAccountFundedEvent.class,
                log, "pay-ledger-organization-account-funded", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.ORGANIZATION_ACCOUNT_FUNDED,
                                event.organizationId(), event.environment(), null, null, null, event.anchorTransferReference(),
                                "EXTERNAL_COLLECTION", event.businessAccountId().toString(),
                                event.amount(), event.currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-payout-completed")
    public void payoutCompleted(String message) {
        processEventIfMatches(message, "PayoutCompletedEvent", PayoutCompletedEvent.class,
                log, "pay-ledger-payout-completed", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.PAYOUT_COMPLETED,
                                event.organizationId(), event.environment(), null, null, null, event.payoutReference(),
                                event.sourceSystem(), event.sourceReferenceId(), event.amount(), event.currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-provider-settled")
    public void providerSettled(String message) {
        processEventIfMatches(message, "ProviderSettlementReceivedEvent",
                ProviderSettlementReceivedEvent.class, log, "pay-ledger-provider-settled",
                e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.PROVIDER_SETTLED,
                                event.organizationId(), event.environment(), null, null, null,
                                event.settlementReference(), "SETTLEMENT", event.provider(),
                                event.amount(), event.currency())));
    }

    private ProcessLedgerEventCommand command(ProcessLedgerEventCommand.Action action, Long organizationId,
                                               String environment, Long outletId, String partyType, String partyReferenceId,
                                               String reference, String sourceSystem, String sourceReferenceId,
                                               BigDecimal amount, String currency) {
        return new ProcessLedgerEventCommand(action, organizationId, environment, outletId, partyType, partyReferenceId,
                reference, sourceSystem, sourceReferenceId, amount, currency);
    }
}
