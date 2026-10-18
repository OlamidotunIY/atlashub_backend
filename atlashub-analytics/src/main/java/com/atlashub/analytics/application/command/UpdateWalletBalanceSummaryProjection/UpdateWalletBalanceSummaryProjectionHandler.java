package com.atlashub.analytics.application.command.UpdateWalletBalanceSummaryProjection;

import com.atlashub.analytics.domain.entities.WalletBalanceSummaryProjection;
import com.atlashub.analytics.domain.repositories.WalletBalanceSummaryProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateWalletBalanceSummaryProjectionHandler extends Command<UpdateWalletBalanceSummaryProjectionCommand, Void> {

    private final WalletBalanceSummaryProjectionRepository repository;

    public UpdateWalletBalanceSummaryProjectionHandler(WalletBalanceSummaryProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateWalletBalanceSummaryProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        WalletBalanceSummaryProjection projection = repository.findByOrgEnv(
                command.organizationId(),
                env
        ).orElseGet(() -> WalletBalanceSummaryProjection.create(
                null,
                command.organizationId(),
                env
        ));

        projection.updateBalance(command.ledgerType(), command.newBalance());
        repository.save(projection);
        return null;
    }
}
