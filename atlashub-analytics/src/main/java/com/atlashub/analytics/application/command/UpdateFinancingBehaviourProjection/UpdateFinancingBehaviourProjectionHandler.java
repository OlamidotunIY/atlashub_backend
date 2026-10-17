package com.atlashub.analytics.application.command.UpdateFinancingBehaviourProjection;

import com.atlashub.analytics.domain.entities.FinancingBehaviourProjection;
import com.atlashub.analytics.domain.repositories.FinancingBehaviourProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateFinancingBehaviourProjectionHandler extends Command<UpdateFinancingBehaviourProjectionCommand, Void> {

    private final FinancingBehaviourProjectionRepository repository;

    public UpdateFinancingBehaviourProjectionHandler(FinancingBehaviourProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateFinancingBehaviourProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        FinancingBehaviourProjection projection = repository.findByOrgEnvAndContextKey(
                command.organizationId(),
                env,
                command.paymentContextKey()
        ).orElseGet(() -> FinancingBehaviourProjection.create(
                null,
                command.organizationId(),
                env,
                command.paymentContextKey()
        ));

        if (command.amount() != null) {
            projection.recordTransaction(command.amount(), command.isInflow(), command.isAnomaly());
        }
        if (command.repaymentRate() != null || command.avgBalance() != null) {
            projection.updateRepaymentRate(command.repaymentRate(), command.avgBalance());
        }

        repository.save(projection);
        return null;
    }
}
