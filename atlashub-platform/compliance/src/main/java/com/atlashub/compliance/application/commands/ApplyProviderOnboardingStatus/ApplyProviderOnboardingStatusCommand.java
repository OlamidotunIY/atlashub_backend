package com.atlashub.compliance.application.commands.ApplyProviderOnboardingStatus;

import java.util.Map;

public record ApplyProviderOnboardingStatusCommand(
        Long onboardingCaseId,
        String status,
        String externalApplicationId,
        String externalMerchantId,
        String externalAccountId,
        String settlementAccountReference,
        Map<String, String> outstandingRequirements,
        String failureCode,
        String failureMessage
) {
}
