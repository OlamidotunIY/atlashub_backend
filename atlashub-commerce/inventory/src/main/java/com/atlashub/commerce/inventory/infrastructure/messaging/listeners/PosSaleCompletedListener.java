package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.DeductReservedStock.DeductReservedStockCommand;
import com.atlashub.commerce.inventory.application.commands.DeductReservedStock.DeductReservedStockHandler;
import com.atlashub.commerce.inventory.infrastructure.messaging.events.PosSaleCompletedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PosSaleCompletedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PosSaleCompletedListener.class);
    private static final String GROUP_ID = "commerce-inventory-sale-completed";

    private final DeductReservedStockHandler handler;

    public PosSaleCompletedListener(ObjectMapper objectMapper, DeductReservedStockHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleCompletedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "PosSaleCompletedEvent",
                PosSaleCompletedPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> handler.execute(new DeductReservedStockCommand(event.payload().salesOrderId()))
        );
    }
}
