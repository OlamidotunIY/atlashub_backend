package com.atlashub.shared.application.port.out;

import com.atlashub.shared.domain.money.CurrencyCode;

public record AccountDetailsDto(
        Long accountId,
        Long integration,
        CurrencyCode currency,
        String status
) {}
