package com.atlashub.logistics.shipping.application.commands.AdvanceShipmentMilestone;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentMilestoneLogRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdvanceShipmentMilestoneHandler extends Command<AdvanceShipmentMilestoneCommand, Void> {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMilestoneLogRepository milestoneLogRepository;

    public AdvanceShipmentMilestoneHandler(
            ShipmentRepository shipmentRepository,
            ShipmentMilestoneLogRepository milestoneLogRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.milestoneLogRepository = milestoneLogRepository;
    }

    @Override
    @Transactional
    public Void execute(AdvanceShipmentMilestoneCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        ZonedDateTime now = ZonedDateTime.now();
        shipment.advanceMilestone(
                input.milestone(),
                input.locationName(),
                input.state(),
                input.description(),
                input.latitude(),
                input.longitude(),
                now,
                input.correlationId()
        );

        Long milestoneLogId = milestoneLogRepository.nextIdentity();
        ShipmentMilestoneLog log = ShipmentMilestoneLog.create(
                milestoneLogId,
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getRouteType(),
                input.milestone(),
                shipment.getStatus(),
                input.locationName(),
                input.state(),
                input.description(),
                input.latitude(),
                input.longitude()
        );

        milestoneLogRepository.save(log);
        shipmentRepository.save(shipment);
        return null;
    }
}
