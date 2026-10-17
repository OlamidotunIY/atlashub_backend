package com.atlashub.analytics.application.command.UpdateProductDemandProjection;

import com.atlashub.analytics.domain.entities.ProductDemandProjection;
import com.atlashub.analytics.domain.repositories.ProductDemandProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateProductDemandProjectionHandler extends Command<UpdateProductDemandProjectionCommand, Void> {

    private final ProductDemandProjectionRepository repository;

    public UpdateProductDemandProjectionHandler(ProductDemandProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateProductDemandProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        ProductDemandProjection projection = repository.findByOrgEnvProductOutletAndMonth(
                command.organizationId(),
                env,
                command.productId(),
                command.outletId(),
                command.month()
        ).orElseGet(() -> ProductDemandProjection.create(
                null,
                command.organizationId(),
                env,
                command.productId(),
                command.outletId(),
                command.month()
        ));

        if (command.isRefund()) {
            projection.applyRefund(command.quantity(), command.revenue());
        } else {
            projection.applySale(command.quantity(), command.revenue());
        }

        repository.save(projection);
        return null;
    }
}
