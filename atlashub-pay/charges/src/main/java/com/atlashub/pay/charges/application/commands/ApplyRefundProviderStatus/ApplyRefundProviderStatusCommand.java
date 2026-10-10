package com.atlashub.pay.charges.application.commands.ApplyRefundProviderStatus;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;

public record ApplyRefundProviderStatusCommand(
        ApiEnvironment environment,
        String transactionReference,
        String refundReference,
        String status,
        Money amount,
        String failureReason
) {
}
