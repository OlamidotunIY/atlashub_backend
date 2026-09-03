package com.atlashub.billing.adapter.in.messaging;

import com.atlashub.billing.application.command.FinalizeInvoicePaymentCommand;
import com.atlashub.billing.application.command.HandleWalletChargeFailedCommand;
import com.atlashub.billing.application.usecase.FinalizeInvoicePaymentUseCase;
import com.atlashub.billing.application.usecase.HandleWalletChargeFailedUseCase;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletChargeResultListener extends BaseKafkaEventListener {

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

    @KafkaListener(topics = "Pay-events", groupId = "billing-module-wallet-group")
    public void onWalletChargeResult(String messagePayload) {
        processEventIfMatches(messagePayload, "WalletChargeSuccessfulEvent", log, "billing-module-wallet-group", root -> {
            Long invoiceId = root.path("payload").path("invoiceId").asLong();
            log.info("Received WalletChargeSuccessfulEvent for invoice {}. Finalizing payment...", invoiceId);
            finalizeInvoicePaymentUseCase.execute(new FinalizeInvoicePaymentCommand(invoiceId));
        });

        processEventIfMatches(messagePayload, "WalletChargeFailedEvent", log, "billing-module-wallet-group", root -> {
            Long invoiceId = root.path("payload").path("invoiceId").asLong();
            String reason = root.path("payload").path("reason").asText("Unknown");
            log.warn("Received WalletChargeFailedEvent for invoice {}. Reason: {}. Switching to external charge.", invoiceId, reason);
            handleWalletChargeFailedUseCase.execute(new HandleWalletChargeFailedCommand(invoiceId, reason));
        });
    }
}