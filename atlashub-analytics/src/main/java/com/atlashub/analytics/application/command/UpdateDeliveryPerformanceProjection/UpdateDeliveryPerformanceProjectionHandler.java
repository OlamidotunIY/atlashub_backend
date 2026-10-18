package com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection;

import com.atlashub.analytics.domain.entities.DeliveryPerformanceProjection;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateDeliveryPerformanceProjectionHandler extends Command<UpdateDeliveryPerformanceProjectionCommand, Void> {

    private final DeliveryPerformanceProjectionRepository repository;

    public UpdateDeliveryPerformanceProjectionHandler(DeliveryPerformanceProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateDeliveryPerformanceProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        DeliveryPerformanceProjection projection = repository.findByOrgEnvAndMonth(
                command.organizationId(),
                env,
                command.month()
        ).orElseGet(() -> DeliveryPerformanceProjection.create(
                null,
                command.organizationId(),
                env,
                command.month()
        ));

        projection.recordShipment(command.isDelivered(), command.isReturned(), command.deliveryMinutes());
        repository.save(projection);
        return null;
    }
}
