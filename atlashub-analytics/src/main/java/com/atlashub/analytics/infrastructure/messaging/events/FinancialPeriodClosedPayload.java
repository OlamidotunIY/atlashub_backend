package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinancialPeriodClosedPayload(
        String eventId,
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
