package com.atlashub.pay.accounts.domain.entities;

import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingAccountDataException;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.events.BusinessDepositAccountActivatedEvent;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessDepositAccountTest {
    @Test
    void activates_only_with_confirmed_number_details() {
        BusinessDepositAccount account = BusinessDepositAccount.request(
                1L, 10L, ApiEnvironment.LIVE, 20L, "customer", CurrencyCode.NGN);
        account.markSubmitted("anchor-account");

        assertThrows(InvalidBankingAccountDataException.class,
                () -> account.activate(new ConfirmedBankingDetails(null, null, null, null, null)));

        account.activate(new ConfirmedBankingDetails(
                "Tolu Store", "1234567890", "******7890", "Anchor Bank", "090000"));
        assertEquals(ExternalAccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.peekDomainEvents().stream()
                .anyMatch(BusinessDepositAccountActivatedEvent.class::isInstance));
    }
}
