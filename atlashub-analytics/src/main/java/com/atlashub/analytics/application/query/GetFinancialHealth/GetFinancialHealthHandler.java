package com.atlashub.analytics.application.query.GetFinancialHealth;

import com.atlashub.analytics.domain.entities.FinancialHealthProjection;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class GetFinancialHealthHandler extends Query<GetFinancialHealthQuery, FinancialHealthResult> {

    private final FinancialHealthProjectionRepository repository;

    public GetFinancialHealthHandler(FinancialHealthProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialHealthResult execute(GetFinancialHealthQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        LocalDate period = query.periodEnd() != null ? query.periodEnd() : LocalDate.now();

        FinancialHealthProjection projection = repository.findByOrgEnvAndPeriodEnd(
                query.organizationId(),
                env,
                period
        ).or(() -> repository.findLatestByOrgEnv(query.organizationId(), env))
        .orElseGet(() -> FinancialHealthProjection.create(
                null,
                query.organizationId(),
                env,
                period
        ));

        return new FinancialHealthResult(
                projection.getOrganizationId(),
                projection.getEnvironment(),
                projection.getPeriodEnd(),
                projection.getRevenue() != null ? projection.getRevenue() : BigDecimal.ZERO,
                projection.getCostOfSales() != null ? projection.getCostOfSales() : BigDecimal.ZERO,
                projection.getGrossProfit() != null ? projection.getGrossProfit() : BigDecimal.ZERO,
                projection.getGrossMargin() != null ? projection.getGrossMargin() : BigDecimal.ZERO,
                projection.getOperatingExpense() != null ? projection.getOperatingExpense() : BigDecimal.ZERO,
                projection.getNetOperatingIncome() != null ? projection.getNetOperatingIncome() : BigDecimal.ZERO,
                projection.getCashBalance() != null ? projection.getCashBalance() : BigDecimal.ZERO,
                projection.getAccountsReceivable() != null ? projection.getAccountsReceivable() : BigDecimal.ZERO,
                projection.getAccountsPayable() != null ? projection.getAccountsPayable() : BigDecimal.ZERO,
                projection.getReconciliationStatus()
        );
    }
}
