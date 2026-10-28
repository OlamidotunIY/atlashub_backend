package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import com.atlashub.authentication.domain.exceptions.InvalidGrantException;
import com.atlashub.authentication.domain.exceptions.InvalidRedirectUriException;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationCodeTest {

    @Test
    void creates_authorization_code_with_valid_parameters() {
        ZonedDateTime expiresAt = ZonedDateTime.now().plusMinutes(5);
        AuthorizationCode code = AuthorizationCode.create(
                100L,
                "code_hash_123",
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid", "profile"),
                "E9Melhoa2OwvFrGMTJguCH5rtG6uEiBQGrA042-EXTguIDA",
                CodeChallengeMethod.S256,
                "nonce_xyz",
                expiresAt
        );

        assertEquals(100L, code.getId());
        assertEquals("code_hash_123", code.getCodeHash());
        assertEquals("atlashub-web", code.getClientId());
        assertEquals(42L, code.getUserId());
        assertEquals("http://localhost:3000/callback", code.getRedirectUri());
        assertFalse(code.isUsed());
        assertFalse(code.isExpired());
    }

    @Test
    void rejects_missing_required_fields() {
        ZonedDateTime expiresAt = ZonedDateTime.now().plusMinutes(5);

        assertThrows(AuthenticationInvariantException.class, () ->
                AuthorizationCode.create(
                        null,
                        "code_hash",
                        "client",
                        1L,
                        "http://localhost/cb",
                        Set.of(),
                        "challenge",
                        CodeChallengeMethod.S256,
                        null,
                        expiresAt
                )
        );

        assertThrows(AuthenticationInvariantException.class, () ->
                AuthorizationCode.create(
                        1L,
                        "",
                        "client",
                        1L,
                        "http://localhost/cb",
                        Set.of(),
                        "challenge",
                        CodeChallengeMethod.S256,
                        null,
                        expiresAt
                )
        );
    }

    @Test
    void markAsUsed_sets_used_and_rejects_subsequent_use() {
        AuthorizationCode code = AuthorizationCode.create(
                100L,
                "code_hash",
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge",
                CodeChallengeMethod.S256,
                null,
                ZonedDateTime.now().plusMinutes(5)
        );

        assertFalse(code.isUsed());
        code.markAsUsed();
        assertTrue(code.isUsed());

        assertThrows(InvalidGrantException.class, code::markAsUsed);
    }

    @Test
    void detects_expiration() {
        AuthorizationCode expiredCode = AuthorizationCode.create(
                100L,
                "code_hash",
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge",
                CodeChallengeMethod.S256,
                null,
                ZonedDateTime.now().minusMinutes(1)
        );

        assertTrue(expiredCode.isExpired());
    }

    @Test
    void validates_redirect_uri_and_client_id() {
        AuthorizationCode code = AuthorizationCode.create(
                100L,
                "code_hash",
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge",
                CodeChallengeMethod.S256,
                null,
                ZonedDateTime.now().plusMinutes(5)
        );

        assertDoesNotThrow(() -> code.validateRedirectUri("http://localhost:3000/callback"));
        assertThrows(InvalidRedirectUriException.class, () -> code.validateRedirectUri("http://localhost:4000/callback"));

        assertDoesNotThrow(() -> code.validateClientId("atlashub-web"));
        assertThrows(InvalidGrantException.class, () -> code.validateClientId("other-client"));
    }
}
