package com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitializeTestPaymentProfileHandler extends Command<InitializeTestPaymentProfileCommand, Void> {
    private final OrganizationProviderProfileRepository repository;
    private final String platformMerchantId;

    public InitializeTestPaymentProfileHandler(OrganizationProviderProfileRepository repository,
            @Value("${atlashub.integrations.paystack.test-merchant-id:atlashub-paystack-test}") String platformMerchantId) {
        this.repository = repository;
        this.platformMerchantId = platformMerchantId;
    }

    @Override
    @Transactional
    public Void execute(InitializeTestPaymentProfileCommand command) {
        repository.findByOrganizationIdAndEnvironmentAndProvider(command.organizationId(), ApiEnvironment.TEST,
                PaymentProvider.PAYSTACK).orElseGet(() -> repository.save(OrganizationProviderProfile.activateTestProfile(
                        repository.nextIdentity(), command.organizationId(), platformMerchantId)));
        return null;
    }
}
