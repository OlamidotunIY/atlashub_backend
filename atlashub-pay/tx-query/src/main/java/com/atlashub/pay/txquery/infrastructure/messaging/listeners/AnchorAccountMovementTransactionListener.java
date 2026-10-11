package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectAccountFunding.ProjectAccountFundingCommand;
import com.atlashub.pay.txquery.application.commands.ProjectAccountFunding.ProjectAccountFundingHandler;
import com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction.ProjectPayoutTransactionCommand;
import com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction.ProjectPayoutTransactionHandler;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.OrganizationAccountDebitedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.OrganizationAccountDebitReversedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class AnchorAccountMovementTransactionListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorAccountMovementTransactionListener.class);
    private static final String GROUP = "pay-txquery-anchor-account-movements";
    private final ProjectPayoutTransactionHandler payoutHandler;
    private final ProjectAccountFundingHandler fundingHandler;

    public AnchorAccountMovementTransactionListener(ObjectMapper mapper,
                                                    ProjectPayoutTransactionHandler payoutHandler,
                                                    ProjectAccountFundingHandler fundingHandler) {
        super(mapper);
        this.payoutHandler = payoutHandler;
        this.fundingHandler = fundingHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("OrganizationAccountDebitedEvent", GROUP);
        registerSubscription("OrganizationAccountDebitReversedEvent", GROUP);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "OrganizationAccountDebitedEvent", OrganizationAccountDebitedEvent.class,
                log, GROUP, error -> error instanceof TimeoutException, event -> {
                    var payload = event.payload();
                    payoutHandler.execute(new ProjectPayoutTransactionCommand(
                            payload.organizationId(), payload.environment(), payload.transferReference(),
                            Money.of(payload.amount(), CurrencyCode.valueOf(payload.currency())),
                            "ANCHOR_TRANSFER", payload.anchorTransferId(), payload.recipientName(),
                            payload.maskedRecipientAccountNumber(), TransactionStatus.SUCCESSFUL,
                            payload.narration(), metadata(payload.recipientBankCode(), payload.anchorTransferId()),
                            payload.completedAt()));
                });
        processEventIfMatches(message, "OrganizationAccountDebitReversedEvent",
                OrganizationAccountDebitReversedEvent.class, log, GROUP,
                error -> error instanceof TimeoutException, event -> {
                    var payload = event.payload();
                    fundingHandler.execute(new ProjectAccountFundingCommand(
                            payload.organizationId(), payload.environment(),
                            "reversal:" + payload.anchorTransferId(),
                            Money.of(payload.amount(), CurrencyCode.valueOf(payload.currency())),
                            payload.businessAccountId(), null, null, "ANCHOR",
                            payload.narration(), metadata(null, payload.anchorTransferId()), payload.reversedAt()));
                });
    }

    private Map<String, String> metadata(String bankCode, String transferId) {
        Map<String, String> metadata = new HashMap<>();
        if (bankCode != null && !bankCode.isBlank()) metadata.put("recipientBankCode", bankCode);
        if (transferId != null && !transferId.isBlank()) metadata.put("anchorTransferId", transferId);
        return Map.copyOf(metadata);
    }
}
