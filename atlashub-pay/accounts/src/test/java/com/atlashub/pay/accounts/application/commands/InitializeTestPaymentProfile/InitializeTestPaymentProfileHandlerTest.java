package com.atlashub.pay.accounts.application.commands.InitializeTestPaymentProfile;

import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InitializeTestPaymentProfileHandlerTest {
    @Test
    void creates_an_active_shared_paystack_test_profile_once() {
        OrganizationProviderProfileRepository repository = mock(OrganizationProviderProfileRepository.class);
        when(repository.findByOrganizationIdAndEnvironmentAndProvider(10L, ApiEnvironment.TEST, PaymentProvider.PAYSTACK))
                .thenReturn(Optional.empty());
        when(repository.nextIdentity()).thenReturn(20L);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        new InitializeTestPaymentProfileHandler(repository, "test-merchant")
                .execute(new InitializeTestPaymentProfileCommand(10L));

        verify(repository).save(argThat(profile -> profile.supports(PaymentCapability.CARD_COLLECTION)
                && profile.supports(PaymentCapability.USSD_COLLECTION)
                && "test-merchant".equals(profile.getExternalMerchantId())));
    }

    @Test
    void leaves_an_existing_test_profile_unchanged() {
        OrganizationProviderProfileRepository repository = mock(OrganizationProviderProfileRepository.class);
        OrganizationProviderProfile existing = OrganizationProviderProfile.activateTestProfile(20L, 10L, "test-merchant");
        when(repository.findByOrganizationIdAndEnvironmentAndProvider(10L, ApiEnvironment.TEST, PaymentProvider.PAYSTACK))
                .thenReturn(Optional.of(existing));

        new InitializeTestPaymentProfileHandler(repository, "test-merchant")
                .execute(new InitializeTestPaymentProfileCommand(10L));

        verify(repository, never()).save(any());
        assertTrue(existing.supports(PaymentCapability.CARD_COLLECTION));
    }
}
