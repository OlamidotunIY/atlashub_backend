package com.atlashub.commerce.inventory.infrastructure.messaging.listeners;

import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveCustomerReturn.ApproveCustomerReturnHandler;
import com.atlashub.commerce.inventory.infrastructure.messaging.events.CustomerReturnShipmentReceivedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class CustomerReturnShipmentReceivedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerReturnShipmentReceivedListener.class);
    private static final String GROUP_ID = "commerce-inventory-return-received";

    private final ApproveCustomerReturnHandler handler;

    public CustomerReturnShipmentReceivedListener(ObjectMapper objectMapper, ApproveCustomerReturnHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ReturnShipmentReceivedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "logistics-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ReturnShipmentReceivedEvent",
                CustomerReturnShipmentReceivedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException,
                event -> handler.execute(new ApproveCustomerReturnCommand(event.payload().returnId())));
    }
}
