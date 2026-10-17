package com.atlashub.analytics.application.command.UpdatePayrollCostProjection;

import com.atlashub.analytics.domain.entities.PayrollCostProjection;
import com.atlashub.analytics.domain.repositories.PayrollCostProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdatePayrollCostProjectionHandler extends Command<UpdatePayrollCostProjectionCommand, Void> {

    private final PayrollCostProjectionRepository repository;

    public UpdatePayrollCostProjectionHandler(PayrollCostProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdatePayrollCostProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        PayrollCostProjection projection = repository.findByOrgEnvAndMonth(
                command.organizationId(),
                env,
                command.month()
        ).orElseGet(() -> PayrollCostProjection.create(
                null,
                command.organizationId(),
                env,
                command.month()
        ));

        projection.applyPayroll(
                command.gross(),
                command.deductions(),
                command.net(),
                command.headcount()
        );

        repository.save(projection);
        return null;
    }
}
