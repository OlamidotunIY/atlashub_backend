package com.atlashub.pay.ledger.application.queries.GetAccountBalance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record AccountBalanceResult(Long accountId, String accountType, BigDecimal balance, String currency, ZonedDateTime asOf) {}
