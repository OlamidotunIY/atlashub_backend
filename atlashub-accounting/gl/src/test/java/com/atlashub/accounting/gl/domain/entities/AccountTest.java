package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.exceptions.SystemAccountImmutableException;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    @Test
    @DisplayName("Should create active account")
    void shouldCreateActiveAccount() {
        Account account = Account.create(
                101L,
                10L,
                "1001",
                "Cash on Hand",
                AccountType.ASSET,
                null,
                false
        );

        assertEquals(101L, account.getId());
        assertEquals(10L, account.getOrganizationId());
        assertEquals("1001", account.getCode());
        assertEquals("Cash on Hand", account.getName());
        assertEquals(AccountType.ASSET, account.getType());
        assertFalse(account.getIsSystemAccount());
        assertTrue(account.getIsActive());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    @DisplayName("Should deactivate and reactivate non-system account")
    void shouldDeactivateAndReactivateNonSystemAccount() {
        Account account = Account.create(
                102L,
                10L,
                "5001",
                "Office Supplies",
                AccountType.EXPENSE,
                null,
                false
        );

        account.deactivate();
        assertFalse(account.getIsActive());

        account.activate();
        assertTrue(account.getIsActive());
    }

    @Test
    @DisplayName("Should throw SystemAccountImmutableException when deactivating system account")
    void shouldThrowWhenDeactivatingSystemAccount() {
        Account account = Account.create(
                103L,
                10L,
                "1000",
                "General Cash",
                AccountType.ASSET,
                null,
                true
        );

        assertThrows(SystemAccountImmutableException.class, account::deactivate);
    }

    @Test
    @DisplayName("Should throw InvalidEntryStateException when creating account with blank code or name")
    void shouldThrowWhenCreatingAccountWithBlankFields() {
        assertThrows(InvalidEntryStateException.class, () ->
                Account.create(104L, 10L, "", "Test", AccountType.ASSET, null, false));

        assertThrows(InvalidEntryStateException.class, () ->
                Account.create(105L, 10L, "1002", "  ", AccountType.ASSET, null, false));
    }
}
