package com.atlashub.accounts.application.port.in;

import com.atlashub.shared.domain.money.CurrencyCode;

public record AccountDetailsDto(
        Long accountId,
        Long integration,
        CurrencyCode currency,
        String status
) {}
