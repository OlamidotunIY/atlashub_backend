package com.atlashub.authentication.infrastructure.persistence.mappers;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.authentication.infrastructure.persistence.entities.AuthorizationCodeJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationCodeMapperTest {

    private AuthorizationCodeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AuthorizationCodeMapper.class);
    }

    @Test
    void maps_domain_to_persistence_and_back() {
        ZonedDateTime now = ZonedDateTime.now();
        AuthorizationCode domain = new AuthorizationCode(
                10L, "hash_123", "atlashub-web", 42L,
                "http://localhost:3000/callback",
                Set.of("openid", "email"),
                "challenge_xyz", CodeChallengeMethod.S256, "nonce_abc",
                now.plusMinutes(5), false, now, now
        );

        AuthorizationCodeJpa jpa = mapper.toPersistence(domain);

        assertNotNull(jpa);
        assertEquals(10L, jpa.getId());
        assertEquals("hash_123", jpa.getCodeHash());
        assertEquals("atlashub-web", jpa.getClientId());
        assertEquals(42L, jpa.getUserId());
        assertEquals("http://localhost:3000/callback", jpa.getRedirectUri());
        assertTrue(jpa.getScopes().contains("openid"));
        assertEquals("challenge_xyz", jpa.getCodeChallenge());
        assertEquals(CodeChallengeMethod.S256, jpa.getCodeChallengeMethod());
        assertEquals("nonce_abc", jpa.getNonce());

        AuthorizationCode mappedDomain = mapper.toDomain(jpa);
        assertNotNull(mappedDomain);
        assertEquals(domain.getId(), mappedDomain.getId());
        assertEquals(domain.getCodeHash(), mappedDomain.getCodeHash());
        assertEquals(domain.getClientId(), mappedDomain.getClientId());
        assertEquals(domain.getUserId(), mappedDomain.getUserId());
    }
}
