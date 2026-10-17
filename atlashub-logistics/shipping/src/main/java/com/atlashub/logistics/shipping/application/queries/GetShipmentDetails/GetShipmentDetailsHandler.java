package com.atlashub.logistics.shipping.application.queries.GetShipmentDetails;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetShipmentDetailsHandler extends Query<GetShipmentDetailsQuery, ShipmentDetailsResult> {

    private final ShipmentRepository shipmentRepository;

    public GetShipmentDetailsHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentDetailsResult execute(GetShipmentDetailsQuery input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        return new ShipmentDetailsResult(
                shipment.getId(),
                shipment.getOrganizationId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                shipment.getType().name(),
                shipment.getServiceTier().name(),
                shipment.getRouteType().name(),
                shipment.getFulfillmentChannel() != null ? shipment.getFulfillmentChannel().name() : null,
                shipment.getOriginAddress(),
                shipment.getDestinationAddress(),
                shipment.getWeightKg(),
                shipment.getDimensions(),
                shipment.getDeclaredValue(),
                shipment.getCustomerShippingFee(),
                shipment.getDeliveryCode(),
                shipment.getDeliveryWindowStart(),
                shipment.getDeliveryWindowEnd(),
                shipment.getCurrentMilestone().name(),
                shipment.getStatus().name(),
                shipment.getDeliveryAttempts(),
                shipment.getFailureFaultType() != null ? shipment.getFailureFaultType().name() : null,
                shipment.getFailureReason(),
                shipment.getRiderId(),
                shipment.getVehicleId(),
                shipment.getCarrier3PLCode(),
                shipment.getProofOfDeliveryUrl(),
                shipment.getCurrentLocation(),
                shipment.getCurrentLatitude(),
                shipment.getCurrentLongitude(),
                shipment.isIncentiveEligible(),
                shipment.getDispatchedAt(),
                shipment.getDeliveredAt(),
                shipment.getCreatedAt()
        );
    }
}
