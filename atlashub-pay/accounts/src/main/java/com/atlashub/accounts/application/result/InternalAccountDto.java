package com.atlashub.accounts.application.result;

import com.atlashub.accounts.domain.valueobject.InternalAccountStatus;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.domain.money.CurrencyCode;

public record InternalAccountDto(
        Long id,
        Long organizationId,
        InternalAccountType type,
        CurrencyCode currency,
        InternalAccountStatus status
) {}
