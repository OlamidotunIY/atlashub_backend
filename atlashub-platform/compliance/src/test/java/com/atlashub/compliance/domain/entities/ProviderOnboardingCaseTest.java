package com.atlashub.compliance.domain.entities;

import com.atlashub.compliance.domain.events.ProviderOnboardingApprovedEvent;
import com.atlashub.compliance.domain.events.ProviderOnboardingStatusChangedEvent;
import com.atlashub.compliance.domain.exception.InvalidProviderOnboardingStateException;
import com.atlashub.compliance.domain.valueobject.ComplianceProvider;
import com.atlashub.compliance.domain.valueobject.ProviderOnboardingStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderOnboardingCaseTest {

    @Test
    void follows_submission_review_and_approval_lifecycle() {
        ProviderOnboardingCase onboarding = ProviderOnboardingCase.request(
                1L, 2L, ApiEnvironment.LIVE, ComplianceProvider.PAYSTACK,
                Set.of("CARD_COLLECTION"));

        onboarding.markSubmitted("application-1");
        onboarding.markUnderReview();
        onboarding.approve("merchant-1", "account-1", "settlement-1");

        assertEquals(ProviderOnboardingStatus.APPROVED, onboarding.getStatus());
        ProviderOnboardingApprovedEvent event = (ProviderOnboardingApprovedEvent)
                onboarding.pullDomainEvents().stream()
                        .filter(ProviderOnboardingApprovedEvent.class::isInstance)
                        .findFirst().orElseThrow();
        assertEquals("merchant-1", event.payload().externalMerchantId());
        assertInstanceOf(ProviderOnboardingApprovedEvent.class, event);
    }

    @Test
    void cannot_approve_before_submission() {
        ProviderOnboardingCase onboarding = ProviderOnboardingCase.request(
                1L, 2L, ApiEnvironment.TEST, ComplianceProvider.PAYSTACK,
                Set.of("USSD_COLLECTION"));

        assertThrows(InvalidProviderOnboardingStateException.class,
                () -> onboarding.approve("merchant-1", null, null));
    }

    @Test
    void publishes_non_approval_status_changes() {
        ProviderOnboardingCase onboarding = ProviderOnboardingCase.request(
                1L, 2L, ApiEnvironment.TEST, ComplianceProvider.PAYSTACK,
                Set.of("CARD_COLLECTION"));

        onboarding.markSubmitted("application-1");
        ProviderOnboardingStatusChangedEvent event = (ProviderOnboardingStatusChangedEvent)
                onboarding.pullDomainEvents().getFirst();

        assertEquals("SUBMITTED", event.payload().status());
        assertEquals(ApiEnvironment.TEST.name(), event.payload().environment());
    }
}
