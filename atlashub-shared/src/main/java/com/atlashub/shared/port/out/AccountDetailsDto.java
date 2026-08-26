package com.atlashub.shared.port.out;

import com.atlashub.shared.money.CurrencyCode;

public record AccountDetailsDto(
        Long accountId,
        Long integration,
        CurrencyCode currency,
        String status
) {}
