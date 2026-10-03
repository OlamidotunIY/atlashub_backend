package com.atlashub.pay.accounts.application.commands.ActivatePaymentProviderProfile;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
public class ActivatePaymentProviderProfileHandler
        extends Command<ActivatePaymentProviderProfileCommand, Void> {
    private final OrganizationProviderProfileRepository repository;

    public ActivatePaymentProviderProfileHandler(OrganizationProviderProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(ActivatePaymentProviderProfileCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        PaymentProvider provider = PaymentProvider.valueOf(command.provider().toUpperCase());
        OrganizationProviderProfile profile = repository
                .findByOrganizationIdAndEnvironmentAndProvider(command.organizationId(), environment, provider)
                .orElseThrow(() -> new IllegalArgumentException("Payment provider profile not found"));
        var capabilities = command.capabilities().stream()
                .map(value -> PaymentCapability.valueOf(value.toUpperCase()))
                .collect(Collectors.toUnmodifiableSet());
        profile.activate(command.onboardingCaseId(), command.externalMerchantId(),
                command.externalAccountId(), command.settlementAccountReference(), capabilities);
        repository.save(profile);
        return null;
    }
}
