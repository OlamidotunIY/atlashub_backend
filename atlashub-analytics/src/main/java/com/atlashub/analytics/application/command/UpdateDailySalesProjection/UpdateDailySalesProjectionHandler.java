package com.atlashub.analytics.application.command.UpdateDailySalesProjection;

import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateDailySalesProjectionHandler extends Command<UpdateDailySalesProjectionCommand, Void> {

    private final DailySalesProjectionRepository repository;

    public UpdateDailySalesProjectionHandler(DailySalesProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateDailySalesProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        DailySalesProjection projection = repository.findByOrgEnvOutletAndDate(
                command.organizationId(),
                env,
                command.outletId(),
                command.date()
        ).orElseGet(() -> DailySalesProjection.create(
                null,
                command.organizationId(),
                env,
                command.outletId(),
                command.date()
        ));

        if (command.isRefund()) {
            projection.applyRefund(
                    command.gross(),
                    command.discount(),
                    command.tax(),
                    command.net(),
                    command.paymentMethod()
            );
        } else {
            projection.applySale(
                    command.gross(),
                    command.discount(),
                    command.tax(),
                    command.net(),
                    command.paymentMethod()
            );
        }

        repository.save(projection);
        return null;
    }
}
