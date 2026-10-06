package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.events.BusinessSubAccountActivatedEvent;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessSubAccountTest {
    @Test
    void provider_existence_can_activate_before_virtual_nuban_details_arrive() {
        BusinessSubAccount account = BusinessSubAccount.request(
                1L, 10L, ApiEnvironment.TEST, 20L, "customer", "fbo", CurrencyCode.NGN);
        account.markSubmitted("anchor-subaccount", null);

        account.activate(new ConfirmedBankingDetails(null, null, null, null, null));

        assertEquals(ExternalAccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.peekDomainEvents().stream()
                .anyMatch(BusinessSubAccountActivatedEvent.class::isInstance));
    }
}
