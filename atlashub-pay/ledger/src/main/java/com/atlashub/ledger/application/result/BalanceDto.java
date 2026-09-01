package com.atlashub.ledger.application.result;

import java.math.BigDecimal;

public record BalanceDto(
    String currency,
    BigDecimal balance
) {}
