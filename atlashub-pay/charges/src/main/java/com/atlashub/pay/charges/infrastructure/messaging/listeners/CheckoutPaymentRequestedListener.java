package com.atlashub.pay.charges.infrastructure.messaging.listeners;

import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeCommand;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeHandler;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.infrastructure.messaging.events.CheckoutPaymentRequestedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

@Component
public class CheckoutPaymentRequestedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CheckoutPaymentRequestedListener.class);
    private static final String GROUP_ID = "pay-charges-commerce-checkout";
    private static final String TOPIC = "commerce-events";
    private static final String EVENT_TYPE = "CheckoutPaymentRequestedEvent";

    private final InitializeChargeHandler handler;

    public CheckoutPaymentRequestedListener(ObjectMapper objectMapper, InitializeChargeHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription(EVENT_TYPE, GROUP_ID);
    }

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, EVENT_TYPE, CheckoutPaymentRequestedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, this::dispatch);
    }

    private void dispatch(CheckoutPaymentRequestedEvent event) {
        if (event == null) {
            return;
        }

        Long orgId = event.resolveOrganizationId();
        ApiEnvironment env = event.resolveEnvironment();
        String ref = event.resolveReference();
        BigDecimal amount = event.resolveAmount();
        CurrencyCode currency = event.resolveCurrency();
        ChargeChannel channel = event.resolveChannel();
        String email = event.resolveEmail();
        String sourceSystem = event.resolveSourceSystem();
        String sourceReferenceId = event.resolveSourceReferenceId();

        if (orgId == null || ref == null || ref.isBlank() || amount == null || email == null || email.isBlank()) {
            log.warn("Ignoring invalid CheckoutPaymentRequestedEvent: missing required fields");
            return;
        }

        InitializeChargeCommand command =
                new InitializeChargeCommand(orgId, env, ref, Money.of(amount, currency), channel, email, sourceSystem,
                        sourceReferenceId, event.resolveCustomerReferenceId(), event.resolveTerminalAssignmentId(),
                        event.resolveMetadata());

        handler.execute(command);
    }
}
