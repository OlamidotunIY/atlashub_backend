package com.atlashub.authentication.infrastructure.persistence.mappers;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.infrastructure.persistence.entities.AuthAccountJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthAccountMapperTest {

    private AuthAccountMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuthAccountMapperImpl();
    }

    @Test
    @DisplayName("Should update fields on existing AuthAccountJpa record")
    void shouldUpdatePersistenceFieldsOnExistingRecord() {
        ZonedDateTime now = ZonedDateTime.now();
        AuthAccountJpa existingRecord = new AuthAccountJpa(
                100L, "user@example.com", "CREDENTIALS", 50L,
                "old-hash", 0, null, null, null,
                now.minusDays(1), now.minusDays(1), 0L
        );

        ZonedDateTime lockedUntil = now.plusMinutes(30);
        ZonedDateTime lastLoginAt = now;
        AuthAccount domain = new AuthAccount(
                100L, "user@example.com", "CREDENTIALS", 50L,
                "new-hash", 3, lockedUntil, lastLoginAt, "127.0.0.1",
                now.minusDays(1), now
        );

        mapper.updatePersistence(domain, existingRecord);

        assertEquals("new-hash", existingRecord.getPassword());
        assertEquals(3, existingRecord.getFailedLoginAttempts());
        assertEquals(lockedUntil, existingRecord.getLockedUntil());
        assertEquals(lastLoginAt, existingRecord.getLastLoginAt());
        assertEquals("127.0.0.1", existingRecord.getLastLoginIp());
    }

    @Test
    @DisplayName("Should map domain AuthAccount to AuthAccountJpa")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        AuthAccount domain = AuthAccount.createCredentialsAccount(200L, 60L, "test@example.com", "hash123");

        AuthAccountJpa jpa = mapper.toPersistence(domain);

        assertNotNull(jpa);
        assertEquals(200L, jpa.getId());
        assertEquals("test@example.com", jpa.getAccountId());
        assertEquals("CREDENTIALS", jpa.getProviderId());
        assertEquals(60L, jpa.getUserId());
        assertEquals("hash123", jpa.getPassword());
    }

    @Test
    @DisplayName("Should map AuthAccountJpa to domain AuthAccount")
    void shouldMapPersistenceToDomain() {
        ZonedDateTime now = ZonedDateTime.now();
        AuthAccountJpa jpa = new AuthAccountJpa(
                300L, "oauth-sub-1", "google", 70L,
                null, 0, null, now, "10.0.0.1",
                now, now, 0L
        );

        AuthAccount domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(300L, domain.getId());
        assertEquals("oauth-sub-1", domain.getAccountId());
        assertEquals("google", domain.getProviderId());
        assertEquals(70L, domain.getUserId());
        assertNull(domain.getPassword());
    }

    @Test
    @DisplayName("Should handle null safely")
    void shouldHandleNullSafely() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toPersistence(null));
    }
}
