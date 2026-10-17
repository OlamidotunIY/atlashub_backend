package com.atlashub.analytics.application.command.UpdateFinancialHealthProjection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFinancialHealthProjectionCommand(
        Long organizationId,
        String environment,
        LocalDate periodEnd,
        BigDecimal revenue,
        BigDecimal costOfSales,
        BigDecimal operatingExpense,
        BigDecimal cashBalance,
        BigDecimal accountsReceivable,
        BigDecimal accountsPayable,
        String reconciliationStatus
) {}
