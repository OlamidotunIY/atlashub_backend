package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import com.atlashub.authentication.domain.exceptions.InvalidClientException;
import com.atlashub.authentication.domain.exceptions.InvalidRedirectUriException;
import com.atlashub.authentication.domain.exceptions.InvalidScopeException;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthClientTest {

    @Test
    void creates_public_client_with_valid_parameters() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid", "profile", "email")
        );

        assertEquals(1L, client.getId());
        assertEquals("atlashub-web", client.getClientId());
        assertTrue(client.isPublic());
        assertTrue(client.isEnabled());
        assertTrue(client.getRedirectUris().contains("http://localhost:3000/callback"));
    }

    @Test
    void rejects_confidential_client_without_secret() {
        assertThrows(AuthenticationInvariantException.class, () ->
                OAuthClient.create(
                        2L,
                        "confidential-app",
                        null,
                        "Confidential App",
                        OAuthClientType.CONFIDENTIAL,
                        Set.of("https://app.example.com/callback"),
                        Set.of("openid")
                )
        );
    }

    @Test
    void rejects_client_without_redirect_uris() {
        assertThrows(AuthenticationInvariantException.class, () ->
                OAuthClient.create(
                        3L,
                        "test-app",
                        null,
                        "Test App",
                        OAuthClientType.PUBLIC,
                        Set.of(),
                        Set.of("openid")
                )
        );
    }

    @Test
    void validates_redirect_uri_successfully() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid")
        );

        assertDoesNotThrow(() -> client.validateRedirectUri("http://localhost:3000/callback"));
        assertThrows(InvalidRedirectUriException.class, () -> client.validateRedirectUri("https://malicious.com/callback"));
    }

    @Test
    void validates_allowed_scopes() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid", "profile", "email")
        );

        assertDoesNotThrow(() -> client.validateScopes(Set.of("openid", "email")));
        assertThrows(InvalidScopeException.class, () -> client.validateScopes(Set.of("openid", "admin_write")));
    }

    @Test
    void validates_client_secret_for_confidential_client() {
        PasswordEncoderPort encoder = new PasswordEncoderPort() {
            @Override
            public String encode(String rawPassword) {
                return "hash_" + rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return ("hash_" + rawPassword).equals(encodedPassword);
            }
        };

        OAuthClient client = OAuthClient.create(
                1L,
                "server-client",
                "hash_super-secret",
                "Server Client",
                OAuthClientType.CONFIDENTIAL,
                Set.of("https://backend.example.com/callback"),
                Set.of("openid")
        );

        assertDoesNotThrow(() -> client.validateSecret("super-secret", encoder));
        assertThrows(InvalidClientException.class, () -> client.validateSecret("wrong-secret", encoder));
    }
}
