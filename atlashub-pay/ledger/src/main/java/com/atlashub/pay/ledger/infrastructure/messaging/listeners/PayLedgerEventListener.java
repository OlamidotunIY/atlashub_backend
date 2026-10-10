package com.atlashub.pay.ledger.infrastructure.messaging.listeners;

import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventCommand;
import com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent.ProcessLedgerEventHandler;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ChargeSuccessfulEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ChargeRefundedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.OrganizationAccountFundedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.PayoutCompletedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ProviderSettlementReceivedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ReservedAccountActivatedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.LedgerInboundEvents.ReservedAccountFundedEvent;
import com.atlashub.pay.ledger.infrastructure.messaging.events.OrganizationRegistered;
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
        registerSubscription("com.atlashub.accounts.domain.events.OrganizationRegistered", "pay-ledger-organization-registered");
        registerSubscription("ReservedAccountActivatedEvent", "pay-ledger-reserved-account-activated");
        registerSubscription("ChargeSuccessfulEvent", "pay-ledger-charge-successful");
        registerSubscription("ChargeRefundedEvent", "pay-ledger-charge-refunded");
        registerSubscription("ReservedAccountFundedEvent", "pay-ledger-reserved-account-funded");
        registerSubscription("OrganizationAccountFundedEvent", "pay-ledger-organization-account-funded");
        registerSubscription("PayoutCompletedEvent", "pay-ledger-payout-completed");
        registerSubscription("ProviderSettlementReceivedEvent", "pay-ledger-provider-settled");
    }

    @KafkaListener(topics = "accounts-events", groupId = "pay-ledger-organization-registered")
    public void organizationRegistered(String message) {
        processEventIfMatches(message, "OrganizationRegistered", OrganizationRegistered.class,
                log, "pay-ledger-organization-registered", e -> e instanceof TimeoutException, event -> {
                    String currency = event.payload().currency();
                    handler.execute(command(ProcessLedgerEventCommand.Action.BOOTSTRAP_ORGANIZATION,
                            event.aggregateId(), "TEST", null, null, null, null, "SYSTEM", null, null, currency));
                    handler.execute(command(ProcessLedgerEventCommand.Action.BOOTSTRAP_ORGANIZATION,
                            event.aggregateId(), "LIVE", null, null, null, null, "SYSTEM", null, null, currency));
                });
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
                                event.payload().organizationId(), event.payload().environment(), null, null, null,
                                event.payload().chargeReference(), "CARD_CHARGE", event.payload().sourceReferenceId(),
                                event.payload().amount().amount(), event.payload().amount().currency().name())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-charge-refunded")
    public void chargeRefunded(String message) {
        processEventIfMatches(message, "ChargeRefundedEvent", ChargeRefundedEvent.class,
                log, "pay-ledger-charge-refunded", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.CHARGE_REFUNDED,
                                event.payload().organizationId(), event.payload().environment(), null, "CUSTOMER",
                                event.payload().customerReferenceId(),
                                "refund:" + event.payload().providerRefundReference(), "COMMERCE_REFUND",
                                event.payload().sourceReferenceId(), event.payload().amount().amount(),
                                event.payload().amount().currency().name())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-reserved-account-funded")
    public void reservedFunded(String message) {
        processEventIfMatches(message, "ReservedAccountFundedEvent", ReservedAccountFundedEvent.class,
                log, "pay-ledger-reserved-account-funded", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.RESERVED_ACCOUNT_FUNDED,
                                event.payload().organizationId(), event.payload().environment(), null,
                                event.payload().ownerType(), event.payload().ownerReferenceId(),
                                event.payload().anchorTransferReference(), "EXTERNAL_COLLECTION",
                                event.payload().reservedAccountId().toString(), event.payload().amount(),
                                event.payload().currency())));
    }

    @KafkaListener(topics = "pay-events", groupId = "pay-ledger-organization-account-funded")
    public void organizationFunded(String message) {
        processEventIfMatches(message, "OrganizationAccountFundedEvent", OrganizationAccountFundedEvent.class,
                log, "pay-ledger-organization-account-funded", e -> e instanceof TimeoutException, event ->
                        handler.execute(command(ProcessLedgerEventCommand.Action.ORGANIZATION_ACCOUNT_FUNDED,
                                event.payload().organizationId(), event.payload().environment(), null, null, null,
                                event.payload().anchorTransferReference(), "EXTERNAL_COLLECTION",
                                event.payload().businessAccountId().toString(), event.payload().amount(),
                                event.payload().currency())));
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
                                event.payload().organizationId(), event.payload().environment(), null, null, null,
                                event.payload().settlementReference(), "SETTLEMENT", event.payload().provider(),
                                event.payload().amount().amount(), event.payload().amount().currency().name())));
    }

    private ProcessLedgerEventCommand command(ProcessLedgerEventCommand.Action action, Long organizationId,
                                               String environment, Long outletId, String partyType, String partyReferenceId,
                                               String reference, String sourceSystem, String sourceReferenceId,
                                               BigDecimal amount, String currency) {
        return new ProcessLedgerEventCommand(action, organizationId, environment, outletId, partyType, partyReferenceId,
                reference, sourceSystem, sourceReferenceId, amount, currency);
    }
}
