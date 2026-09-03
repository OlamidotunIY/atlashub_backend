package com.atlashub.charges.adapter.in.messaging;

import com.atlashub.charges.application.command.InitiateExternalChargeCommand;
import com.atlashub.charges.application.usecase.InitiateExternalChargeUseCase;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ExternalChargeRequestedListener extends BaseKafkaEventListener {

    private final InitiateExternalChargeUseCase useCase;

    public ExternalChargeRequestedListener(InitiateExternalChargeUseCase useCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.useCase = useCase;
    }

    @KafkaListener(topics = "Billing-events", groupId = "charges-module-group")
    public void onExternalChargeRequested(String messagePayload) {
        processEventIfMatches(messagePayload, "ExternalChargeRequestedEvent", log, "charges-module-group", root -> {
            Long invoiceId = root.path("payload").path("invoiceId").asLong();
            Long organizationId = root.path("payload").path("organizationId").asLong();
            BigDecimal amount = new BigDecimal(root.path("payload").path("amount").asText());
            String currency = root.path("payload").path("currency").asText();
            String customerEmail = root.path("payload").path("customerEmail").asText();
            String redirectUrl = root.path("payload").path("redirectUrl").asText();

            log.info("Received ExternalChargeRequestedEvent for invoice {}. Initiating Paystack charge...", invoiceId);
            useCase.execute(new InitiateExternalChargeCommand(invoiceId, organizationId, amount, currency, customerEmail, redirectUrl));
        });
    }
}