package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionTest {

    @Test
    void normalizes_the_atlashub_environment() {
        Session session = Session.create(
                1L, "token-hash", "2", 3L, "test", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), "127.0.0.1", "browser");

        assertEquals("TEST", session.getEnvironment());
    }

    @Test
    void rejects_an_expired_session() {
        assertThrows(AuthenticationInvariantException.class, () -> Session.create(
                1L, "token-hash", "2", 3L, "TEST", "fingerprint", "family",
                ZonedDateTime.now().minusSeconds(1), null, null));
    }
}
