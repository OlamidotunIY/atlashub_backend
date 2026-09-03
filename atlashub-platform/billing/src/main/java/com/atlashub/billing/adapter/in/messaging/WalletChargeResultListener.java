package com.atlashub.billing.adapter.in.messaging;

import com.atlashub.billing.application.command.FinalizeInvoicePaymentCommand;
import com.atlashub.billing.application.command.HandleWalletChargeFailedCommand;
import com.atlashub.billing.application.usecase.FinalizeInvoicePaymentUseCase;
import com.atlashub.billing.application.usecase.HandleWalletChargeFailedUseCase;
import com.atlashub.billing.domain.events.WalletChargeSuccessfulEvent;
import com.atlashub.billing.domain.events.WalletChargeFailedEvent;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletChargeResultListener extends BaseKafkaEventListener {

    private static final String GROUP_ID = "billing-module-wallet-group";

    private final FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase;
    private final HandleWalletChargeFailedUseCase handleWalletChargeFailedUseCase;

    public WalletChargeResultListener(
            FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase,
            HandleWalletChargeFailedUseCase handleWalletChargeFailedUseCase,
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.finalizeInvoicePaymentUseCase = finalizeInvoicePaymentUseCase;
        this.handleWalletChargeFailedUseCase = handleWalletChargeFailedUseCase;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "Pay-events", groupId = GROUP_ID)
    public void onWalletChargeResult(String messagePayload) {
        processEventIfMatches(messagePayload, "WalletChargeSuccessfulEvent", WalletChargeSuccessfulEvent.class, log, GROUP_ID, event -> {
            Long invoiceId = event.payload().invoiceId();
            log.info("Received WalletChargeSuccessfulEvent for invoice {}. Finalizing payment...", invoiceId);
            finalizeInvoicePaymentUseCase.execute(new FinalizeInvoicePaymentCommand(invoiceId));
        });

        processEventIfMatches(messagePayload, "WalletChargeFailedEvent", WalletChargeFailedEvent.class, log, GROUP_ID, event -> {
            Long invoiceId = event.payload().invoiceId();
            String reason = event.payload().reason();
            if (reason == null) reason = "Unknown";
            log.warn("Received WalletChargeFailedEvent for invoice {}. Reason: {}. Switching to external charge.", invoiceId, reason);
            handleWalletChargeFailedUseCase.execute(new HandleWalletChargeFailedCommand(invoiceId, reason));
        });
    }
}