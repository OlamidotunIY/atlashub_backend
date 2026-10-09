package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.PaymentProviderProfileActivatedEvent;
import com.atlashub.pay.accounts.domain.events.ProviderOnboardingRequestedEvent;
import com.atlashub.pay.accounts.domain.exceptions.InvalidProviderProfileStateException;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganizationProviderProfileTest {
    @Test
    void activates_shared_test_profile_without_provider_onboarding() {
        OrganizationProviderProfile profile = OrganizationProviderProfile.activateTestProfile(1L, 10L, "platform-test");

        assertTrue(profile.supports(PaymentCapability.CARD_COLLECTION));
        assertTrue(profile.supports(PaymentCapability.USSD_COLLECTION));
        assertEquals("platform-test", profile.getExternalMerchantId());
    }


    @Test
    void requests_only_new_capabilities_and_activates_approved_capabilities() {
        OrganizationProviderProfile profile = OrganizationProviderProfile.request(
                1L, 2L, ApiEnvironment.TEST, PaymentProvider.PAYSTACK,
                Set.of(PaymentCapability.CARD_COLLECTION), null);

        assertInstanceOf(ProviderOnboardingRequestedEvent.class, profile.pullDomainEvents().getFirst());

        profile.requestCapabilities(Set.of(
                PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION));
        ProviderOnboardingRequestedEvent request = (ProviderOnboardingRequestedEvent)
                profile.pullDomainEvents().getFirst();
        assertEquals(Set.of("USSD_COLLECTION"), request.payload().capabilities());

        profile.activate(10L, "merchant-1", "account-1", "settlement-1",
                Set.of(PaymentCapability.CARD_COLLECTION, PaymentCapability.USSD_COLLECTION));

        assertEquals(ProviderProfileStatus.ACTIVE, profile.getStatus());
        assertTrue(profile.supports(PaymentCapability.CARD_COLLECTION));
        assertInstanceOf(PaymentProviderProfileActivatedEvent.class, profile.pullDomainEvents().getFirst());
    }

    @Test
    void refuses_to_activate_an_unrequested_capability() {
        OrganizationProviderProfile profile = OrganizationProviderProfile.request(
                1L, 2L, ApiEnvironment.LIVE, PaymentProvider.PAYSTACK,
                Set.of(PaymentCapability.CARD_COLLECTION), null);

        assertThrows(InvalidProviderProfileStateException.class, () -> profile.activate(
                10L, "merchant-1", null, null, Set.of(PaymentCapability.USSD_COLLECTION)));
        assertFalse(profile.supports(PaymentCapability.CARD_COLLECTION));
    }

    @Test
    void reflects_non_approval_provider_statuses() {
        OrganizationProviderProfile profile = OrganizationProviderProfile.request(
                1L, 2L, ApiEnvironment.LIVE, PaymentProvider.PAYSTACK,
                Set.of(PaymentCapability.CARD_COLLECTION), null);

        profile.applyOnboardingStatus("INFORMATION_REQUIRED", null, "Upload director identity");
        assertEquals(ProviderProfileStatus.INFORMATION_REQUIRED, profile.getStatus());

        profile.applyOnboardingStatus("ERROR", "PROVIDER_TIMEOUT", "Try again later");
        assertEquals(ProviderProfileStatus.FAILED, profile.getStatus());
        assertEquals("PROVIDER_TIMEOUT", profile.getFailureCode());
    }

    @Test
    void keeps_existing_capabilities_active_while_an_addition_is_reviewed() {
        OrganizationProviderProfile profile = OrganizationProviderProfile.request(
                1L, 2L, ApiEnvironment.LIVE, PaymentProvider.PAYSTACK,
                Set.of(PaymentCapability.CARD_COLLECTION), null);
        profile.activate(10L, "merchant-1", null, null,
                Set.of(PaymentCapability.CARD_COLLECTION));
        profile.pullDomainEvents();

        profile.requestCapabilities(Set.of(PaymentCapability.USSD_COLLECTION));
        profile.applyOnboardingStatus("INFORMATION_REQUIRED", null, "Additional requirement");

        assertEquals(ProviderProfileStatus.ACTIVE, profile.getStatus());
        assertTrue(profile.supports(PaymentCapability.CARD_COLLECTION));
        assertFalse(profile.supports(PaymentCapability.USSD_COLLECTION));
    }
}
