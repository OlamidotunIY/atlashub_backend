package com.atlashub.ledger.application.dto;

import java.math.BigDecimal;

public record BalanceDto(
    String currency,
    BigDecimal balance
) {}
