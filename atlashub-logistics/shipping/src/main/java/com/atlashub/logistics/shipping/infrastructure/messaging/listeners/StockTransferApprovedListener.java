package com.atlashub.logistics.shipping.infrastructure.messaging.listeners;

import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentCommand;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentHandler;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.infrastructure.messaging.events.StockTransferApprovedPayload;
import java.math.BigDecimal;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockTransferApprovedListener {

    private final CreateShipmentHandler createShipmentHandler;

    public StockTransferApprovedListener(CreateShipmentHandler createShipmentHandler) {
        this.createShipmentHandler = createShipmentHandler;
    }

    @KafkaListener(topics = "commerce-events", groupId = "logistics-shipping-stock-transfer-approved")
    public void onStockTransferApproved(StockTransferApprovedPayload payload) {
        CreateShipmentCommand command = new CreateShipmentCommand(
                payload.organizationId(),
                null,
                ShipmentType.INTER_OUTLET_TRANSFER,
                DeliveryTier.STANDARD,
                payload.sourceAddress(),
                payload.destinationAddress(),
                payload.requestedBy(),
                payload.organizationId(),
                payload.totalWeightKg(),
                new Dimensions(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                payload.declaredValue(),
                payload.correlationId()
        );

        createShipmentHandler.execute(command);
    }
}
