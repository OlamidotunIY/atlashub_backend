package com.atlashub.pay.ledger.application.queries.GetAccountBalance;

import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record AccountBalanceResult(
        Long accountId,
        LedgerAccountType accountType,
        BigDecimal balance,
        String currency,
        ZonedDateTime asOf) {}
