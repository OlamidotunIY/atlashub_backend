package com.atlashub.logistics.shipping.application.commands.RecordFailedDeliveryAttempt;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordFailedDeliveryAttemptHandler extends Command<RecordFailedDeliveryAttemptCommand, Void> {

    private final ShipmentRepository shipmentRepository;

    public RecordFailedDeliveryAttemptHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public Void execute(RecordFailedDeliveryAttemptCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        ZonedDateTime now = ZonedDateTime.now();
        shipment.recordFailedAttempt(input.reason(), now, input.correlationId());

        if (input.markFailedFinal()) {
            shipment.markFailed(input.faultType(), input.reason(), now, input.correlationId());
            shipment.initiateReturn("Return to origin after failed delivery: " + input.reason(), now, input.correlationId());
        }

        shipmentRepository.save(shipment);
        return null;
    }
}
