package com.atlashub.logistics.shipping.infrastructure.messaging.listeners;

import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentCommand;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentHandler;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.infrastructure.messaging.events.OnlineOrderCreatedPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OnlineOrderCreatedListener {

    private final CreateShipmentHandler createShipmentHandler;

    public OnlineOrderCreatedListener(CreateShipmentHandler createShipmentHandler) {
        this.createShipmentHandler = createShipmentHandler;
    }

    @KafkaListener(topics = "commerce-events", groupId = "logistics-shipping-online-order-created")
    public void onOrderCreated(OnlineOrderCreatedPayload payload) {
        CreateShipmentCommand command = new CreateShipmentCommand(
                payload.organizationId(),
                payload.orderId(),
                ShipmentType.CUSTOMER_ORDER,
                payload.serviceTier(),
                payload.originAddress(),
                payload.destinationAddress(),
                payload.organizationId(),
                payload.customerId(),
                payload.weightKg(),
                payload.dimensions(),
                payload.declaredValue(),
                payload.correlationId()
        );

        createShipmentHandler.execute(command);
    }
}
