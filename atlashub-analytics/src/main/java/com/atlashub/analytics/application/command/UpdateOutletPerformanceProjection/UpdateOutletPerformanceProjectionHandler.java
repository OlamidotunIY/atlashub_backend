package com.atlashub.analytics.application.command.UpdateOutletPerformanceProjection;

import com.atlashub.analytics.domain.entities.OutletPerformanceProjection;
import com.atlashub.analytics.domain.repositories.OutletPerformanceProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class UpdateOutletPerformanceProjectionHandler extends Command<UpdateOutletPerformanceProjectionCommand, Void> {

    private final OutletPerformanceProjectionRepository repository;

    public UpdateOutletPerformanceProjectionHandler(OutletPerformanceProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateOutletPerformanceProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        OutletPerformanceProjection projection = repository.findByOrgEnvOutletAndMonth(
                command.organizationId(),
                env,
                command.outletId(),
                command.month()
        ).orElseGet(() -> OutletPerformanceProjection.create(
                null,
                command.organizationId(),
                env,
                command.outletId(),
                command.month()
        ));

        if (command.sales() != null && command.sales().compareTo(BigDecimal.ZERO) > 0) {
            projection.applySales(command.sales(), command.discounts(), command.count());
        }
        if (command.expense() != null && command.expense().compareTo(BigDecimal.ZERO) > 0) {
            projection.applyExpense(command.expense());
        }

        repository.save(projection);
        return null;
    }
}
