package com.atlashub.analytics.application.command.UpdateInventoryHealthProjection;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateInventoryHealthProjectionHandler extends Command<UpdateInventoryHealthProjectionCommand, Void> {

    private final InventoryHealthProjectionRepository repository;

    public UpdateInventoryHealthProjectionHandler(InventoryHealthProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateInventoryHealthProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        InventoryHealthProjection projection = repository.findByOrgEnvOutletAndProduct(
                command.organizationId(),
                env,
                command.outletId(),
                command.productId()
        ).orElseGet(() -> InventoryHealthProjection.create(
                null,
                command.organizationId(),
                env,
                command.outletId(),
                command.productId()
        ));

        projection.updateStock(
                command.currentStock(),
                command.reorderPoint(),
                command.coverageDays(),
                command.inventoryValue(),
                command.deadStock()
        );

        repository.save(projection);
        return null;
    }
}
