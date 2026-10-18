package com.atlashub.logistics.shipping.application.commands.CompleteDelivery;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CompleteDeliveryHandler extends Command<CompleteDeliveryCommand, Void> {

    private final ShipmentRepository shipmentRepository;
    private final DispatchRiderRepository riderRepository;
    private final ShipmentMilestoneLogRepository milestoneLogRepository;

    public CompleteDeliveryHandler(
            ShipmentRepository shipmentRepository,
            DispatchRiderRepository riderRepository,
            ShipmentMilestoneLogRepository milestoneLogRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.riderRepository = riderRepository;
        this.milestoneLogRepository = milestoneLogRepository;
    }

    @Override
    @Transactional
    public Void execute(CompleteDeliveryCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        ZonedDateTime now = ZonedDateTime.now();
        shipment.completeDelivery(input.deliveryCode(), input.proofOfDeliveryUrl(), now, input.correlationId());

        if (shipment.getRiderId() != null) {
            riderRepository.findById(shipment.getRiderId()).ifPresent(rider -> {
                rider.completeDelivery();
                riderRepository.save(rider);
            });
        }

        Long logId = milestoneLogRepository.nextIdentity();
        ShipmentMilestoneLog log = ShipmentMilestoneLog.create(
                logId,
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getRouteType(),
                TrackingMilestone.DELIVERED,
                shipment.getStatus(),
                shipment.getDestinationAddress().city() + ", " + shipment.getDestinationAddress().state(),
                shipment.getDestinationAddress().state(),
                "Handover confirmed via delivery code and proof of delivery",
                null,
                null
        );
        milestoneLogRepository.save(log);

        shipmentRepository.save(shipment);
        return null;
    }
}
