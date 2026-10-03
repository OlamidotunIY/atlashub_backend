package com.atlashub.pay.accounts.application.commands.ApplyPaymentProviderOnboardingStatus;

public record ApplyPaymentProviderOnboardingStatusCommand(
        Long onboardingCaseId,
        Long organizationId,
        String environment,
        String provider,
        String status,
        String failureCode,
        String failureMessage
) {
}
