package com.atlashub.billing.adapter.in.messaging;

import com.atlashub.billing.application.command.FinalizeInvoicePaymentCommand;
import com.atlashub.billing.application.usecase.FinalizeInvoicePaymentUseCase;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalPaymentResultListener extends BaseKafkaEventListener {

    private final FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase;

    public ExternalPaymentResultListener(
            FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase,
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.finalizeInvoicePaymentUseCase = finalizeInvoicePaymentUseCase;
    }

    @KafkaListener(topics = "Pay-events", groupId = "billing-module-external-group")
    public void onExternalPaymentResult(String messagePayload) {
        processEventIfMatches(messagePayload, "PaymentSuccessfulEvent", log, "billing-module-external-group", root -> {
            String purpose = root.path("payload").path("purpose").asText();
            if ("PLATFORM_INVOICE".equals(purpose)) {
                String metadata = root.path("payload").path("metadata").asText();
                try {
                    Long invoiceId = Long.valueOf(metadata);
                    log.info("Received PaymentSuccessfulEvent for invoice {}. Finalizing payment...", invoiceId);
                    finalizeInvoicePaymentUseCase.execute(new FinalizeInvoicePaymentCommand(invoiceId));
                } catch (NumberFormatException e) {
                    log.error("Failed to parse invoiceId from metadata: {}", metadata);
                }
            }
        });
    }
}