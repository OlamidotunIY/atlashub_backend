package com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetShipmentDeliveryCodeHandler extends Query<GetShipmentDeliveryCodeQuery, DeliveryCodeResult> {

    private final ShipmentRepository shipmentRepository;

    public GetShipmentDeliveryCodeHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryCodeResult execute(GetShipmentDeliveryCodeQuery input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        return new DeliveryCodeResult(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getDeliveryCode(),
                shipment.getStatus().name()
        );
    }
}
