package com.atlashub.compliance.application.commands.RequestProviderOnboarding;

import java.util.Set;

public record RequestProviderOnboardingCommand(
        Long organizationId,
        String environment,
        String provider,
        Set<String> capabilities
) {
}
