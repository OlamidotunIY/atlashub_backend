package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockCommand;
import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockHandler;
import com.atlashub.commerce.inventory.infrastructure.messaging.events.PosSaleFailedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PosSaleFailedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PosSaleFailedListener.class);
    private static final String GROUP_ID = "commerce-inventory-sale-failed";

    private final ReleaseReservedStockHandler handler;

    public PosSaleFailedListener(ObjectMapper objectMapper, ReleaseReservedStockHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleFailedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "PosSaleFailedEvent",
                PosSaleFailedPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> handler.execute(new ReleaseReservedStockCommand(event.payload().salesOrderId()))
        );
    }
}
