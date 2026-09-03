package com.atlashub.billing.adapter.in.messaging;

import com.atlashub.billing.application.command.FinalizeInvoicePaymentCommand;
import com.atlashub.billing.application.usecase.FinalizeInvoicePaymentUseCase;
import com.atlashub.billing.domain.events.PaymentSuccessfulEvent;
import com.atlashub.billing.domain.valueobject.ChargePurpose;
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
public class ExternalPaymentResultListener extends BaseKafkaEventListener {

    private static final String GROUP_ID = "billing-module-external-group";

    private final FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase;

    public ExternalPaymentResultListener(
            FinalizeInvoicePaymentUseCase finalizeInvoicePaymentUseCase,
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.finalizeInvoicePaymentUseCase = finalizeInvoicePaymentUseCase;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "Pay-events", groupId = GROUP_ID)
    public void onExternalPaymentResult(String messagePayload) {
        processEventIfMatches(messagePayload, "PaymentSuccessfulEvent", PaymentSuccessfulEvent.class, log, GROUP_ID, event -> {
            ChargePurpose purpose = event.payload().purpose();
            if (purpose == ChargePurpose.PLATFORM_INVOICE) {
                String metadata = event.payload().metadata();
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