package com.atlashub.compliance.application.commands.ApplyProviderOnboardingStatus;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.domain.repositories.ProviderOnboardingCaseRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplyProviderOnboardingStatusHandler extends Command<ApplyProviderOnboardingStatusCommand, Void> {
    private final ProviderOnboardingCaseRepository repository;

    public ApplyProviderOnboardingStatusHandler(ProviderOnboardingCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(ApplyProviderOnboardingStatusCommand command) {
        ProviderOnboardingCase onboarding = repository.findById(command.onboardingCaseId())
                .orElseThrow(() -> new IllegalArgumentException("Provider onboarding case not found"));
        switch (command.status().toUpperCase()) {
            case "INFORMATION_REQUIRED" -> onboarding.requireInformation(command.outstandingRequirements());
            case "SUBMITTED" -> onboarding.markSubmitted(command.externalApplicationId());
            case "UNDER_REVIEW" -> onboarding.markUnderReview();
            case "APPROVED" -> onboarding.approve(
                    command.externalMerchantId(), command.externalAccountId(),
                    command.settlementAccountReference());
            case "REJECTED" -> onboarding.reject(command.failureCode(), command.failureMessage());
            case "SUSPENDED" -> onboarding.suspend(command.failureCode(), command.failureMessage());
            case "ERROR" -> onboarding.recordError(command.failureCode(), command.failureMessage());
            default -> throw new IllegalArgumentException("Unsupported provider onboarding status");
        }
        repository.save(onboarding);
        return null;
    }
}
