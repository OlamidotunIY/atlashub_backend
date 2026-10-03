package com.atlashub.pay.accounts.application.commands.ActivatePaymentProviderProfile;

import java.util.Set;

public record ActivatePaymentProviderProfileCommand(
        Long onboardingCaseId,
        Long organizationId,
        String environment,
        String provider,
        Set<String> capabilities,
        String externalMerchantId,
        String externalAccountId,
        String settlementAccountReference
) {
}
