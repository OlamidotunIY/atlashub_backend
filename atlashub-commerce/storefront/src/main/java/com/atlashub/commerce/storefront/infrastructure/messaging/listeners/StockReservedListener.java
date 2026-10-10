package com.atlashub.commerce.storefront.infrastructure.messaging.listeners;

import com.atlashub.commerce.storefront.application.commands.HandleStockReserved.HandleStockReservedCommand;
import com.atlashub.commerce.storefront.application.commands.HandleStockReserved.HandleStockReservedHandler;
import com.atlashub.commerce.storefront.infrastructure.messaging.events.StockReservedPayload;
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
public class StockReservedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockReservedListener.class);
    private static final String GROUP_ID = "commerce-storefront-stock-reserved";

    private final HandleStockReservedHandler handler;

    public StockReservedListener(ObjectMapper objectMapper, HandleStockReservedHandler handler) {
        super(objectMapper);
        this.handler = Objects.requireNonNull(handler, "HandleStockReservedHandler must not be null");
    }

    @PostConstruct
    public void init() {
        registerSubscription("StockReservedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "StockReservedEvent",
                StockReservedPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> handler.execute(new HandleStockReservedCommand(event.payload().salesOrderId()))
        );
    }
}
