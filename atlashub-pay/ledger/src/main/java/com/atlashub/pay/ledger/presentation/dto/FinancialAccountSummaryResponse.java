package com.atlashub.pay.ledger.presentation.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

public record FinancialAccountSummaryResponse(
        Long id,
        String businessName,
        String name,
        String type,
        String scope,
        String currency,
        String status,
        boolean frozen,
        Set<String> restrictions,
        BigDecimal ledgerBalance,
        BigDecimal availableBalance,
        BigDecimal pendingSettlementBalance,
        ZonedDateTime balanceAsOf,
        ZonedDateTime createdAt
) {
}
