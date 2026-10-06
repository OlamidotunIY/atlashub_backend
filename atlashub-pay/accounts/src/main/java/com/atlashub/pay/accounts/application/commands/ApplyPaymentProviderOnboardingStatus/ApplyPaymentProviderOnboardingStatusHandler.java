package com.atlashub.pay.accounts.application.commands.ApplyPaymentProviderOnboardingStatus;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplyPaymentProviderOnboardingStatusHandler
        extends Command<ApplyPaymentProviderOnboardingStatusCommand, Void> {
    private final OrganizationProviderProfileRepository repository;

    public ApplyPaymentProviderOnboardingStatusHandler(OrganizationProviderProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(ApplyPaymentProviderOnboardingStatusCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        PaymentProvider provider = PaymentProvider.valueOf(command.provider().toUpperCase());
        OrganizationProviderProfile profile = repository
                .findByOrganizationIdAndEnvironmentAndProvider(command.organizationId(), environment, provider)
                .orElseThrow(() -> new IllegalArgumentException("Payment provider profile not found"));
        profile.applyOnboardingStatus(command.status(), command.failureCode(), command.failureMessage());
        repository.save(profile);
        return null;
    }
}
