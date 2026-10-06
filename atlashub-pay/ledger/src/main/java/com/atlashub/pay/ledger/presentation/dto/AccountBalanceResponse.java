package com.atlashub.pay.ledger.presentation.dto;

import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record AccountBalanceResponse(
        Long accountId,
        LedgerAccountType accountType,
        BigDecimal balance,
        String currency,
        ZonedDateTime asOf) {}
