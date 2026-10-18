package com.atlashub.billing.infrastructure.messaging.listeners;

import com.atlashub.billing.application.command.MarkInvoicePaid.MarkInvoicePaidCommand;
import com.atlashub.billing.application.command.MarkInvoicePaid.MarkInvoicePaidHandler;
import com.atlashub.billing.application.command.RecordTransactionVolume.RecordTransactionVolumeCommand;
import com.atlashub.billing.application.command.RecordTransactionVolume.RecordTransactionVolumeHandler;
import com.atlashub.billing.infrastructure.messaging.events.ChargeSuccessfulKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

@Component
public class PaymentSuccessfulListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentSuccessfulListener.class);
    private static final String GROUP_ID = "billing-payment-successful-group";

    private final MarkInvoicePaidHandler markInvoicePaidHandler;
    private final RecordTransactionVolumeHandler recordTransactionVolumeHandler;

    public PaymentSuccessfulListener(
            ObjectMapper objectMapper,
            MarkInvoicePaidHandler markInvoicePaidHandler,
            RecordTransactionVolumeHandler recordTransactionVolumeHandler
    ) {
        super(objectMapper);
        this.markInvoicePaidHandler = markInvoicePaidHandler;
        this.recordTransactionVolumeHandler = recordTransactionVolumeHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeSuccessfulEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ChargeSuccessfulEvent", ChargeSuccessfulKafkaEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            ChargeSuccessfulKafkaEvent.Payload payload = event.payload();
            if (payload == null) {
                return;
            }

            // 1. If sourceSystem is PLATFORM_BILLING, mark invoice paid
            if ("PLATFORM_BILLING".equalsIgnoreCase(payload.sourceSystem()) && payload.sourceReferenceId() != null) {
                try {
                    Long invoiceId = Long.parseLong(payload.sourceReferenceId());
                    markInvoicePaidHandler.execute(new MarkInvoicePaidCommand(
                            invoiceId,
                            payload.chargeReference(),
                            ZonedDateTime.now()
                    ));
                } catch (NumberFormatException ex) {
                    log.warn("Invalid sourceReferenceId on billing payment: {}", payload.sourceReferenceId());
                }
            }

            // 2. Track monthly transaction volume for tier calculations
            if (payload.organizationId() != null && payload.amount() != null) {
                String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                BigDecimal amount = payload.amount().amount();
                String currency = payload.amount().currency() != null ? payload.amount().currency() : "NGN";
                recordTransactionVolumeHandler.execute(new RecordTransactionVolumeCommand(
                        payload.organizationId(),
                        currentMonth,
                        amount,
                        currency
                ));
            }
        });
    }
}
