package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.events.AuthAccountLocked;
import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthAccountTest {

    @Test
    void locks_after_five_failed_logins_and_publishes_the_lock_event() {
        AuthAccount account = AuthAccount.createCredentialsAccount(
                1L, 2L, "TOLU@example.com", "bcrypt-hash");

        for (int attempt = 0; attempt < 5; attempt++) account.recordFailedLogin();

        assertTrue(account.isLocked());
        assertEquals("tolu@example.com", account.getAccountId());
        assertInstanceOf(AuthAccountLocked.class, account.pullDomainEvents().getFirst());
    }

    @Test
    void rejects_missing_credential_identity() {
        assertThrows(AuthenticationInvariantException.class,
                () -> AuthAccount.createCredentialsAccount(1L, 2L, " ", "bcrypt-hash"));
    }
}
