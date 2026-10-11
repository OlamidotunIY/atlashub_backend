package com.atlashub.pay.ledger.presentation.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

public record FinancialAccountSummaryResponse(
        Long id,
        String name,
        String type,
        String scope,
        String currency,
        String status,
        Set<String> restrictions,
        BigDecimal balance,
        ZonedDateTime balanceAsOf,
        ZonedDateTime createdAt
) {
}
