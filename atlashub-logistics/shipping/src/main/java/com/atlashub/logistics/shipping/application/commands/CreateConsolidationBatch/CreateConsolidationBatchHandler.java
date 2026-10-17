package com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch;

import com.atlashub.logistics.shipping.domain.entities.ConsolidationBatch;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.ConsolidationBatchRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateConsolidationBatchHandler extends Command<CreateConsolidationBatchCommand, CreateConsolidationBatchResult> {

    private final ConsolidationBatchRepository batchRepository;
    private final ShipmentRepository shipmentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CreateConsolidationBatchHandler(
            ConsolidationBatchRepository batchRepository,
            ShipmentRepository shipmentRepository
    ) {
        this.batchRepository = batchRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public CreateConsolidationBatchResult execute(CreateConsolidationBatchCommand input) {
        Long batchId = batchRepository.nextIdentity();
        String batchReference = "BATCH-" + System.currentTimeMillis() + "-" + (100 + secureRandom.nextInt(900));

        com.atlashub.shared.domain.valueobject.CurrencyCode currencyCode = com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        if (input.currency() != null && !input.currency().isBlank()) {
            try {
                currencyCode = com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(input.currency().trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        ConsolidationBatch batch = ConsolidationBatch.create(
                batchId,
                batchReference,
                input.originState(),
                input.originCity(),
                input.destinationState(),
                input.destinationCity(),
                currencyCode
        );

        List<Long> addedShipmentIds = new ArrayList<>();
        if (input.shipmentIds() != null) {
            for (Long shipmentId : input.shipmentIds()) {
                Shipment shipment = shipmentRepository.findById(shipmentId)
                        .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

                batch.addShipment(
                        shipment.getId(),
                        shipment.getWeightKg(),
                        shipment.getBaselineSingleCost(),
                        shipment.getLatestDispatchAt()
                );
                shipment.assignToBatch(batchId);
                shipmentRepository.save(shipment);
                addedShipmentIds.add(shipmentId);
            }
        }

        batchRepository.save(batch);

        return new CreateConsolidationBatchResult(
                batch.getId(),
                batch.getBatchReference(),
                batch.getRouteType().name(),
                addedShipmentIds,
                batch.getTotalWeightKg(),
                batch.getTotalBaselineCost(),
                batch.getLatestDispatchAt()
        );
    }
}
