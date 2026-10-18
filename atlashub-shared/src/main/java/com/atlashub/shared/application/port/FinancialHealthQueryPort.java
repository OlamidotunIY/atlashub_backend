package com.atlashub.shared.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface FinancialHealthQueryPort {

    record FinancialHealthSnapshot(
            Long organizationId,
            String environment,
            LocalDate periodEnd,
            BigDecimal revenue,
            BigDecimal costOfSales,
            BigDecimal grossProfit,
            BigDecimal grossMargin,
            BigDecimal operatingExpense,
            BigDecimal netOperatingIncome,
            BigDecimal cashBalance,
            BigDecimal accountsReceivable,
            BigDecimal accountsPayable,
            String reconciliationStatus
    ) {}

    Optional<FinancialHealthSnapshot> getFinancialHealth(Long organizationId, String environment, LocalDate periodEnd);
}
