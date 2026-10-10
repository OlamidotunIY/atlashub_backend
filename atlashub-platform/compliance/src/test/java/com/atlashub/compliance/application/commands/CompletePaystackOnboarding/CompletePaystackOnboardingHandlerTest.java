package com.atlashub.compliance.application.commands.CompletePaystackOnboarding;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.domain.events.ProviderOnboardingApprovedEvent;
import com.atlashub.compliance.domain.repositories.ProviderOnboardingCaseRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.domain.valueobject.ProviderOnboardingStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompletePaystackOnboardingHandlerTest {
    @Test
    void approvesConfirmedPaystackRouteThroughExistingOnboardingCase() {
        ProviderOnboardingCaseRepository repository = mock(ProviderOnboardingCaseRepository.class);
        ProviderOnboardingCase onboarding = ProviderOnboardingCase.request(
                20L, 10L, ApiEnvironment.LIVE, ComplianceProvider.PAYSTACK,
                Set.of("CARD_COLLECTION", "USSD_COLLECTION"));
        when(repository.findByOrganizationIdAndEnvironmentAndProvider(
                10L, ApiEnvironment.LIVE, ComplianceProvider.PAYSTACK)).thenReturn(Optional.of(onboarding));

        new CompletePaystackOnboardingHandler(repository).execute(new CompletePaystackOnboardingCommand(
                10L, "LIVE", "PAYSTACK", Set.of("CARD_COLLECTION", "USSD_COLLECTION"),
                "ACCT_sub", "090", "30"));

        assertEquals(ProviderOnboardingStatus.APPROVED, onboarding.getStatus());
        assertInstanceOf(ProviderOnboardingApprovedEvent.class, onboarding.pullDomainEvents().getLast());
        verify(repository).save(onboarding);
    }
}
