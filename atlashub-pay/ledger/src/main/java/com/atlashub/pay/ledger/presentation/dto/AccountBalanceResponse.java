package com.atlashub.pay.ledger.presentation.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record AccountBalanceResponse(
        Long accountId, String accountType, BigDecimal balance, String currency, ZonedDateTime asOf) {}
