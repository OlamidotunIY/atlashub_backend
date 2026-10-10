package com.atlashub.pay.charges.application.commands.ApplyChargeProviderStatus;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;

public record ApplyChargeProviderStatusCommand(
        ApiEnvironment environment,
        String reference,
        String providerReference,
        Money confirmedAmount,
        CurrencyCode confirmedCurrency,
        boolean successful,
        String failureReason
) {
}
