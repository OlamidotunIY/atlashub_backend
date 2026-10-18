package com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateTransactionVolumeProjectionHandler extends Command<UpdateTransactionVolumeProjectionCommand, Void> {

    private final TransactionVolumeProjectionRepository repository;

    public UpdateTransactionVolumeProjectionHandler(TransactionVolumeProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateTransactionVolumeProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        TransactionVolumeProjection projection = repository.findByOrgEnvAndMonth(
                command.organizationId(),
                env,
                command.month()
        ).orElseGet(() -> TransactionVolumeProjection.create(
                null,
                command.organizationId(),
                env,
                command.month()
        ));

        if (command.isCharge()) {
            projection.applyCharge(command.amount(), command.platformFee(), command.isSuccessful());
        }
        if (command.isPayout()) {
            projection.applyPayout(command.amount());
        }

        repository.save(projection);
        return null;
    }
}
