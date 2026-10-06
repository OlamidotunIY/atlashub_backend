package com.atlashub.pay.accounts.application.commands.EnablePaymentCapability;

public record EnablePaymentCapabilityResult(
        Long providerProfileId,
        String capability,
        String status
) {
}
