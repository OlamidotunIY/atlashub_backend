package com.atlashub.compliance.application.commands.RequestProviderOnboarding;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.repositories.ProviderOnboardingCaseRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.compliance.domain.exception.InvalidProviderOnboardingStateException;
import org.springframework.stereotype.Component;

@Component
public class RequestProviderOnboardingHandler extends Command<RequestProviderOnboardingCommand, Void> {
    private final ComplianceRecordRepository complianceRepository;
    private final ProviderOnboardingCaseRepository onboardingRepository;

    public RequestProviderOnboardingHandler(
            ComplianceRecordRepository complianceRepository,
            ProviderOnboardingCaseRepository onboardingRepository
    ) {
        this.complianceRepository = complianceRepository;
        this.onboardingRepository = onboardingRepository;
    }

    @Override
    public Void execute(RequestProviderOnboardingCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        ComplianceProvider provider = ComplianceProvider.valueOf(command.provider());

        if (environment == ApiEnvironment.LIVE) {
            var compliance = complianceRepository.findByOrganizationId(command.organizationId())
                    .orElseThrow(() -> new InvalidProviderOnboardingStateException("AtlasHub compliance must be completed before live provider onboarding"));
            if (compliance.getStatus() != ComplianceStatus.APPROVED) {
                throw new InvalidProviderOnboardingStateException("AtlasHub compliance must be approved before live provider onboarding");
            }
        }

        var existing = onboardingRepository.findByOrganizationIdAndEnvironmentAndProvider(
                command.organizationId(), environment, provider).orElse(null);
        if (existing != null) {
            existing.requestCapabilities(command.capabilities());
            onboardingRepository.save(existing);
            return null;
        }

        onboardingRepository.save(ProviderOnboardingCase.request(
                onboardingRepository.nextIdentity(), command.organizationId(), environment,
                provider, command.capabilities()));
        return null;
    }
}
