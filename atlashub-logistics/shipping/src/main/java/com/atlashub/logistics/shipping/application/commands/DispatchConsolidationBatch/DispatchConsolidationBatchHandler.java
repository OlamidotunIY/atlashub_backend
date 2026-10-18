package com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch;

import com.atlashub.logistics.shipping.domain.entities.ConsolidationBatch;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ConsolidationBatchNotFoundException;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ConsolidationBatchRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DispatchConsolidationBatchHandler extends Command<DispatchConsolidationBatchCommand, DispatchConsolidationBatchResult> {

    private final ConsolidationBatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;

    public DispatchConsolidationBatchHandler(
            ConsolidationBatchRepository batchRepository,
            ShipmentRepository shipmentRepository
    ) {
        this.batchRepository = batchRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public DispatchConsolidationBatchResult execute(DispatchConsolidationBatchCommand input) {
        ConsolidationBatch batch = batchRepository.findById(input.batchId())
                .orElseThrow(() -> new ConsolidationBatchNotFoundException(input.batchId()));

        ZonedDateTime now = ZonedDateTime.now();
        batch.dispatchBatch(input.channel(), input.carrierCode(), input.actualBatchCost(), now);
        batchRepository.save(batch);

        for (Long shipmentId : batch.getShipmentIdsView()) {
            Shipment shipment = shipmentRepository.findById(shipmentId)
                    .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

            com.atlashub.shared.domain.valueobject.Money allocatedCost = batch.allocateCostForShipment(shipment.getWeightKg());
            shipment.setAllocatedCost(allocatedCost);

            TrackingMilestone milestone = shipment.getRouteType() == RouteType.INTER_STATE
                    ? TrackingMilestone.CONSOLIDATED_FOR_LINEHAUL
                    : TrackingMilestone.PICKED_UP_FROM_STORE;

            shipment.advanceMilestone(
                    milestone,
                    batch.getOriginCity() + ", " + batch.getOriginState(),
                    batch.getOriginState(),
                    "Dispatched via consolidation batch " + batch.getBatchReference(),
                    null,
                    null,
                    now,
                    input.correlationId()
            );

            shipmentRepository.save(shipment);
        }

        return new DispatchConsolidationBatchResult(
                batch.getId(),
                batch.getBatchReference(),
                batch.getStatus().name(),
                batch.getShipmentIdsView().size(),
                batch.getActualBatchCost(),
                batch.getDispatchedAt()
        );
    }
}
