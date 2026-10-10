package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentHandler;
import com.atlashub.commerce.storefront.infrastructure.messaging.events.StockReservationFailedPayload;
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
public class StockReservationFailedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockReservationFailedListener.class);
    private static final String GROUP_ID = "commerce-storefront-stock-failed";

    private final FailPaymentHandler handler;

    public StockReservationFailedListener(ObjectMapper objectMapper, FailPaymentHandler handler) {
        super(objectMapper);
        this.handler = Objects.requireNonNull(handler, "FailPaymentHandler must not be null");
    }

    @PostConstruct
    public void init() {
        registerSubscription("StockReservationFailedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "StockReservationFailedEvent",
                StockReservationFailedPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> handler.execute(new FailPaymentCommand(event.payload().salesOrderId(), event.payload().failureReason()))
        );
    }
}
