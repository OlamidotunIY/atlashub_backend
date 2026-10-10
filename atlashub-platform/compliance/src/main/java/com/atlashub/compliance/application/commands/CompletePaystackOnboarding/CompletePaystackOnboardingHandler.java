package com.atlashub.compliance.application.commands.CompletePaystackOnboarding;

import com.atlashub.compliance.domain.exception.InvalidProviderOnboardingStateException;
import com.atlashub.compliance.domain.repositories.ProviderOnboardingCaseRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class CompletePaystackOnboardingHandler extends Command<CompletePaystackOnboardingCommand, Void> {
    private final ProviderOnboardingCaseRepository cases;

    public CompletePaystackOnboardingHandler(ProviderOnboardingCaseRepository cases) {
        this.cases = cases;
    }

    @Override
    public Void execute(CompletePaystackOnboardingCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        ComplianceProvider provider = ComplianceProvider.valueOf(command.provider());
        var onboarding = cases.findByOrganizationIdAndEnvironmentAndProvider(
                        command.organizationId(), environment, provider)
                .orElseThrow(() -> new InvalidProviderOnboardingStateException(
                        "Provider onboarding case must exist before route confirmation"));
        onboarding.requestCapabilities(command.capabilities());
        onboarding.markSubmitted(command.externalMerchantId());
        onboarding.markUnderReview();
        onboarding.approve(command.externalMerchantId(), command.externalAccountId(),
                command.settlementAccountReference());
        cases.save(onboarding);
        return null;
    }
}
