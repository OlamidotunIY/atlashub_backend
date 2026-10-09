package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.OrganizationBankingActivatedEvent;
import com.atlashub.pay.accounts.domain.events.OrganizationBankingProvisioningFailedEvent;
import com.atlashub.pay.accounts.domain.valueobject.BankingProfileStatus;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationBankingProfileTest {
    @Test
    void activates_once_after_deposit_account_is_linked() {
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(
                1L, 10L, ApiEnvironment.TEST, "sandbox-customer");
        profile.linkDepositAccount(20L);

        profile.activate();
        profile.activate();

        assertEquals(BankingProfileStatus.ACTIVE, profile.getStatus());
        assertTrue(profile.isUsable());
        assertEquals(1, profile.peekDomainEvents().stream()
                .filter(OrganizationBankingActivatedEvent.class::isInstance).count());
    }

    @Test
    void remains_blocked_until_every_restriction_is_removed() {
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(
                1L, 10L, ApiEnvironment.LIVE, "live-customer");
        profile.linkDepositAccount(20L);
        profile.activate();
        profile.restrict(BankingRestrictionType.COMPLIANCE);
        profile.restrict(BankingRestrictionType.RISK);

        profile.removeRestriction(BankingRestrictionType.COMPLIANCE);

        assertEquals(BankingProfileStatus.SUSPENDED, profile.getStatus());
        assertFalse(profile.isUsable());
    }

    @Test
    void publishes_terminal_provisioning_failure() {
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(
                1L, 10L, ApiEnvironment.LIVE, "live-customer");

        profile.fail("DEPOSIT_FAILED", "Rejected");

        assertEquals(BankingProfileStatus.FAILED, profile.getStatus());
        assertTrue(profile.peekDomainEvents().stream()
                .anyMatch(OrganizationBankingProvisioningFailedEvent.class::isInstance));
    }
}
