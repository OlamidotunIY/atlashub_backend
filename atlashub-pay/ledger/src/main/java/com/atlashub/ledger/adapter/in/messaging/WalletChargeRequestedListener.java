package com.atlashub.ledger.adapter.in.messaging;

import com.atlashub.ledger.application.command.ProcessWalletChargeCommand;
import com.atlashub.ledger.application.usecase.ProcessWalletChargeUseCase;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WalletChargeRequestedListener extends BaseKafkaEventListener {

    private final ProcessWalletChargeUseCase useCase;

    public WalletChargeRequestedListener(ProcessWalletChargeUseCase useCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.useCase = useCase;
    }

    @KafkaListener(topics = "Billing-events", groupId = "ledger-module-wallet-group")
    public void onWalletChargeRequested(String messagePayload) {
        processEventIfMatches(messagePayload, "WalletChargeRequestedEvent", log, "ledger-module-wallet-group", root -> {
            Long invoiceId = root.path("payload").path("invoiceId").asLong();
            Long organizationId = root.path("payload").path("organizationId").asLong();
            BigDecimal amount = new BigDecimal(root.path("payload").path("amount").asText());
            String currency = root.path("payload").path("currency").asText();

            log.info("Received WalletChargeRequestedEvent for invoice {}. Processing wallet charge...", invoiceId);
            useCase.execute(new ProcessWalletChargeCommand(invoiceId, organizationId, amount, currency));
        });
    }
}