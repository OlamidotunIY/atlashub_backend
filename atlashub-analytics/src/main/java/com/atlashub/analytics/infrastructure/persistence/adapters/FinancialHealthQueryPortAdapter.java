package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.application.query.GetFinancialHealth.FinancialHealthResult;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthHandler;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthQuery;
import com.atlashub.shared.application.port.FinancialHealthQueryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class FinancialHealthQueryPortAdapter implements FinancialHealthQueryPort {

    private final GetFinancialHealthHandler handler;

    public FinancialHealthQueryPortAdapter(GetFinancialHealthHandler handler) {
        this.handler = handler;
    }

    @Override
    public Optional<FinancialHealthSnapshot> getFinancialHealth(Long organizationId, String environment, LocalDate periodEnd) {
        if (organizationId == null) {
            return Optional.empty();
        }
        FinancialHealthResult result = handler.execute(
                new GetFinancialHealthQuery(organizationId, environment, periodEnd)
        );
        return Optional.of(new FinancialHealthSnapshot(
                result.organizationId(),
                result.environment(),
                result.periodEnd(),
                result.revenue(),
                result.costOfSales(),
                result.grossProfit(),
                result.grossMargin(),
                result.operatingExpense(),
                result.netOperatingIncome(),
                result.cashBalance(),
                result.accountsReceivable(),
                result.accountsPayable(),
                result.reconciliationStatus()
        ));
    }
}
