package com.atlashub.logistics.shipping.application.commands.UpdateShipmentLocation;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateShipmentLocationHandler extends Command<UpdateShipmentLocationCommand, Void> {

    private final ShipmentRepository shipmentRepository;

    public UpdateShipmentLocationHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public Void execute(UpdateShipmentLocationCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        shipment.updateLiveLocation(
                input.locationName(),
                input.latitude(),
                input.longitude(),
                ZonedDateTime.now(),
                input.correlationId()
        );

        shipmentRepository.save(shipment);
        return null;
    }
}
