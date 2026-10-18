package com.atlashub.analytics.application.command.UpdateFinancialHealthProjection;

import com.atlashub.analytics.domain.entities.FinancialHealthProjection;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateFinancialHealthProjectionHandler extends Command<UpdateFinancialHealthProjectionCommand, Void> {

    private final FinancialHealthProjectionRepository repository;

    public UpdateFinancialHealthProjectionHandler(FinancialHealthProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateFinancialHealthProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        FinancialHealthProjection projection = repository.findByOrgEnvAndPeriodEnd(
                command.organizationId(),
                env,
                command.periodEnd()
        ).orElseGet(() -> FinancialHealthProjection.create(
                null,
                command.organizationId(),
                env,
                command.periodEnd()
        ));

        projection.updateMetrics(
                command.revenue(),
                command.costOfSales(),
                command.operatingExpense(),
                command.cashBalance(),
                command.accountsReceivable(),
                command.accountsPayable(),
                command.reconciliationStatus()
        );

        repository.save(projection);
        return null;
    }
}
