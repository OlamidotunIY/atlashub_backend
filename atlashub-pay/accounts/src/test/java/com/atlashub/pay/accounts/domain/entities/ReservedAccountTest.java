package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.events.ReservedAccountActivatedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountRequestedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountReactivatedEvent;
import com.atlashub.pay.accounts.domain.events.ReservedAccountSuspendedEvent;
import com.atlashub.pay.accounts.domain.valueobject.*;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservedAccountTest {
    @Test
    void provider_activation_does_not_bypass_an_active_compliance_restriction() {
        ReservedAccount account = ReservedAccount.request(1L, 10L, ApiEnvironment.LIVE,
                ReservedAccountOwnerType.CUSTOMER, "customer-1", 20L, "anchor-subaccount",
                "NINEPSB", "request-1", CurrencyCode.NGN);
        assertTrue(account.peekDomainEvents().stream().anyMatch(ReservedAccountRequestedEvent.class::isInstance));
        account.markSubmitted("reserved-1", "anchor-customer");
        account.restrict(BankingRestrictionType.COMPLIANCE);
        assertTrue(account.peekDomainEvents().stream().anyMatch(ReservedAccountSuspendedEvent.class::isInstance));

        account.activate(new ConfirmedBankingDetails(
                "Ada Doe", "1234567890", "******7890", "9PSB", "090001"));

        assertEquals(ExternalAccountStatus.SUSPENDED, account.getStatus());
        assertEquals(1, account.peekDomainEvents().stream()
                .filter(ReservedAccountActivatedEvent.class::isInstance).count());
        account.removeRestriction(BankingRestrictionType.COMPLIANCE);
        assertEquals(ExternalAccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.peekDomainEvents().stream().anyMatch(ReservedAccountReactivatedEvent.class::isInstance));
    }
}
