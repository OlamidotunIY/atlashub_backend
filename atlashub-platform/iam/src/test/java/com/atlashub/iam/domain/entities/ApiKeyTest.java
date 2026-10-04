package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.ApiKeyRevokedEvent;
import com.atlashub.iam.domain.exception.ApiKeyAlreadyRevokedException;
import com.atlashub.iam.domain.exception.InvalidApiKeyException;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyTest {
    @Test
    void creates_and_revokes_key_with_event() {
        ApiKey key = ApiKey.create(1L, 2L, "atlas_pk_test_key", "ciphertext", "Server", ApiEnvironment.TEST, null);
        key.revoke(3L);
        assertTrue(key.getRevoked());
        assertInstanceOf(ApiKeyRevokedEvent.class, key.pullDomainEvents().getFirst());
        assertThrows(ApiKeyAlreadyRevokedException.class, () -> key.revoke(3L));
    }

    @Test
    void rejects_missing_key_material() {
        assertThrows(InvalidApiKeyException.class,
                () -> ApiKey.create(1L, 2L, "", "ciphertext", "Server", ApiEnvironment.TEST, null));
    }
}
