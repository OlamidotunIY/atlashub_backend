package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.CompletePayment.CompletePaymentCommand;
import com.atlashub.commerce.storefront.application.commands.CompletePayment.CompletePaymentHandler;
import com.atlashub.commerce.storefront.infrastructure.messaging.events.ChargeSuccessfulPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Component
public class ChargeSuccessfulListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChargeSuccessfulListener.class);
    private static final String GROUP_ID = "commerce-payment-group";

    private final CompletePaymentHandler handler;

    public ChargeSuccessfulListener(ObjectMapper objectMapper, CompletePaymentHandler handler) {
        super(objectMapper);
        this.handler = Objects.requireNonNull(handler, "CompletePaymentHandler must not be null");
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeSuccessfulEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "ChargeSuccessfulEvent",
                ChargeSuccessfulPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> {
                    Long orderId = event.payload().orderId();
                    if (orderId != null) {
                        handler.execute(new CompletePaymentCommand(orderId));
                    }
                }
        );
    }
}
