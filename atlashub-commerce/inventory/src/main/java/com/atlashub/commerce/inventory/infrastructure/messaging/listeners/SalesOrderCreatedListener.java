package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.OrderItemDto;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderCommand;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderHandler;
import com.atlashub.commerce.inventory.infrastructure.messaging.events.SalesOrderCreatedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class SalesOrderCreatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderCreatedListener.class);
    private static final String GROUP_ID = "commerce-inventory-order-created";

    private final ReserveStockForOrderHandler handler;

    public SalesOrderCreatedListener(ObjectMapper objectMapper, ReserveStockForOrderHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("SalesOrderCreatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(
                messagePayload,
                "SalesOrderCreatedEvent",
                SalesOrderCreatedPayload.class,
                log,
                GROUP_ID,
                e -> e instanceof TimeoutException,
                event -> {
                    List<OrderItemDto> items = event.payload().items() != null
                            ? event.payload().items().stream()
                            .map(i -> new OrderItemDto(i.productId(), i.variantId(), i.quantity()))
                            .toList()
                            : Collections.emptyList();

                    handler.execute(new ReserveStockForOrderCommand(
                            event.payload().salesOrderId(),
                            event.payload().organizationId(),
                            event.payload().outletId(),
                            items
                    ));
                }
        );
    }
}
