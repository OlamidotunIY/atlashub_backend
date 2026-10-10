package com.atlashub.compliance.application.commands.CompletePaystackOnboarding;

import java.util.Set;

public record CompletePaystackOnboardingCommand(
        Long organizationId,
        String environment,
        String provider,
        Set<String> capabilities,
        String externalMerchantId,
        String externalAccountId,
        String settlementAccountReference
) {
}
