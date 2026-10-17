package com.atlashub.logistics.shipping.application.commands.Route3PL;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidShipmentStatusException;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.shared.application.port.ThirdPartyCarrierPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Route3PLHandler extends Command<Route3PLCommand, Void> {

    private final ShipmentRepository shipmentRepository;
    private final ThirdPartyCarrierPort thirdPartyCarrierPort;

    public Route3PLHandler(
            ShipmentRepository shipmentRepository,
            ThirdPartyCarrierPort thirdPartyCarrierPort
    ) {
        this.shipmentRepository = shipmentRepository;
        this.thirdPartyCarrierPort = thirdPartyCarrierPort;
    }

    @Override
    @Transactional
    public Void execute(Route3PLCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        ThirdPartyCarrierPort.CarrierBookingRequest request = new ThirdPartyCarrierPort.CarrierBookingRequest(
                shipment.getTrackingNumber(),
                shipment.getOriginAddress().street(),
                shipment.getOriginAddress().state(),
                shipment.getOriginAddress().city(),
                shipment.getDestinationAddress().street(),
                shipment.getDestinationAddress().state(),
                shipment.getDestinationAddress().city(),
                shipment.getWeightKg(),
                shipment.getDeclaredValue(),
                shipment.getServiceTier() == DeliveryTier.URGENT
        );

        ThirdPartyCarrierPort.CarrierBookingResult result = thirdPartyCarrierPort.bookShipment(
                input.carrierCode(),
                request
        );

        if (!result.successful()) {
            throw new InvalidShipmentStatusException("3PL carrier booking failed: " + result.errorMessage());
        }

        Money actualCost = result.actualCost() != null
                ? result.actualCost()
                : shipment.getBaselineSingleCost();

        shipment.route3PL(
                input.carrierCode(),
                result.carrierReference(),
                actualCost,
                input.correlationId()
        );

        shipmentRepository.save(shipment);
        return null;
    }
}
