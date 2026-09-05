package com.atlashub.ledger.adapter.in.messaging;

import com.atlashub.ledger.application.command.HandleWalletChargeCommand;
import com.atlashub.ledger.application.usecase.HandleWalletChargeUseCase;
import com.atlashub.ledger.domain.event.WalletChargeRequestedEvent;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WalletChargeRequestedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(WalletChargeRequestedListener.class);
    private static final String GROUP_ID = "ledger-module-wallet-group";

    private final HandleWalletChargeUseCase useCase;

    public WalletChargeRequestedListener(
            HandleWalletChargeUseCase useCase, 
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.useCase = useCase;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "billing-events", groupId = GROUP_ID)
    public void onWalletChargeRequested(String messagePayload) {
        processEventIfMatches(messagePayload, "WalletChargeRequestedEvent", WalletChargeRequestedEvent.class, log, GROUP_ID, event -> {
            var payload = event.payload();
            log.info("Received WalletChargeRequestedEvent for invoice {}. Processing wallet charge...", payload.invoiceId());

            HandleWalletChargeCommand command = new HandleWalletChargeCommand(
                    payload.invoiceId(),
                    payload.organizationId(),
                    payload.amount(),
                    CurrencyCode.valueOf(payload.currency())
            );

            useCase.execute(command);
        });
    }
}
