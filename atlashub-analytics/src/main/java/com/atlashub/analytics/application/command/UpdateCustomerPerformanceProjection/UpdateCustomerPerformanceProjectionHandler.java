package com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection;

import com.atlashub.analytics.domain.entities.CustomerPerformanceProjection;
import com.atlashub.analytics.domain.repositories.CustomerPerformanceProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateCustomerPerformanceProjectionHandler extends Command<UpdateCustomerPerformanceProjectionCommand, Void> {

    private final CustomerPerformanceProjectionRepository repository;

    public UpdateCustomerPerformanceProjectionHandler(CustomerPerformanceProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateCustomerPerformanceProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        CustomerPerformanceProjection projection = repository.findByOrgEnvCustomerAndMonth(
                command.organizationId(),
                env,
                command.customerId(),
                command.month()
        ).orElseGet(() -> CustomerPerformanceProjection.create(
                null,
                command.organizationId(),
                env,
                command.customerId(),
                command.month()
        ));

        if (command.isRefund()) {
            projection.applyRefund(command.amount());
        } else {
            projection.applyOrder(command.amount());
        }

        repository.save(projection);
        return null;
    }
}
