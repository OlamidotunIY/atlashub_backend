package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerAccountFrozenEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountUnfrozenEvent;
import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.pay.ledger.domain.valueobject.*;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LedgerAccountTest {
    @Test
    void scopes_account_to_environment_and_preserves_independent_restrictions() {
        LedgerAccount account = LedgerAccount.create(1L, 2L, ApiEnvironment.TEST,
                LedgerAccountType.OPERATING, null, null, null,
                CurrencyCode.NGN, NormalBalance.CREDIT);

        account.freeze(LedgerRestrictionType.COMPLIANCE);
        account.freeze(LedgerRestrictionType.ORGANIZATION_BAN);
        account.unfreeze(LedgerRestrictionType.COMPLIANCE);

        assertEquals(ApiEnvironment.TEST, account.getEnvironment());
        assertEquals(LedgerAccountStatus.FROZEN, account.getStatus());
        assertTrue(account.getActiveRestrictions().contains(LedgerRestrictionType.ORGANIZATION_BAN));
        assertTrue(account.peekDomainEvents().stream().anyMatch(LedgerAccountFrozenEvent.class::isInstance));

        account.unfreeze(LedgerRestrictionType.ORGANIZATION_BAN);
        assertEquals(LedgerAccountStatus.ACTIVE, account.getStatus());
        assertTrue(account.peekDomainEvents().stream().anyMatch(LedgerAccountUnfrozenEvent.class::isInstance));
    }

    @Test
    void enforces_party_and_outlet_ownership_shapes() {
        assertThrows(LedgerInvariantException.class, () -> LedgerAccount.create(
                1L, 2L, ApiEnvironment.LIVE, LedgerAccountType.CUSTOMER_FUNDS,
                null, null, null, CurrencyCode.NGN, NormalBalance.CREDIT));
        assertThrows(LedgerInvariantException.class, () -> LedgerAccount.create(
                1L, 2L, ApiEnvironment.LIVE, LedgerAccountType.TILL,
                null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT));
    }
}
