package com.atlashub.logistics.shipping.application.queries.TrackShipment;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TrackShipmentHandler extends Query<TrackShipmentQuery, ShipmentTrackingResult> {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMilestoneLogRepository milestoneLogRepository;

    public TrackShipmentHandler(
            ShipmentRepository shipmentRepository,
            ShipmentMilestoneLogRepository milestoneLogRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.milestoneLogRepository = milestoneLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentTrackingResult execute(TrackShipmentQuery input) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(input.trackingNumber())
                .orElseThrow(() -> new ShipmentNotFoundException(input.trackingNumber()));

        List<ShipmentMilestoneLog> logs = milestoneLogRepository.findByTrackingNumberOrderByOccurredAtAsc(input.trackingNumber());

        List<ShipmentTrackingResult.MilestoneEntry> history = logs.stream()
                .map(log -> new ShipmentTrackingResult.MilestoneEntry(
                        log.getMilestone().name(),
                        log.getStatus().name(),
                        log.getLocationName(),
                        log.getState(),
                        log.getDescription(),
                        log.getLatitude(),
                        log.getLongitude(),
                        log.getOccurredAt()
                ))
                .toList();

        return new ShipmentTrackingResult(
                shipment.getTrackingNumber(),
                shipment.getStatus().name(),
                shipment.getCurrentMilestone().name(),
                shipment.getRouteType().name(),
                shipment.getCurrentLocation(),
                shipment.getCurrentLatitude(),
                shipment.getCurrentLongitude(),
                shipment.getDeliveryWindowStart(),
                shipment.getDeliveryWindowEnd(),
                history
        );
    }
}
