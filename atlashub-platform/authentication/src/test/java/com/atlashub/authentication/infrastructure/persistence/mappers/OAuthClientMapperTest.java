package com.atlashub.authentication.infrastructure.persistence.mappers;

import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.authentication.infrastructure.persistence.entities.OAuthClientJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthClientMapperTest {

    private OAuthClientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(OAuthClientMapper.class);
    }

    @Test
    void maps_domain_to_persistence_and_back() {
        ZonedDateTime now = ZonedDateTime.now();
        OAuthClient domain = new OAuthClient(
                1L, "atlashub-web", "secret_hash", "AtlasHub Web",
                OAuthClientType.CONFIDENTIAL,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid", "email"),
                true, now, now
        );

        OAuthClientJpa jpa = mapper.toPersistence(domain);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals("atlashub-web", jpa.getClientId());
        assertEquals("secret_hash", jpa.getClientSecret());
        assertEquals("AtlasHub Web", jpa.getClientName());
        assertEquals(OAuthClientType.CONFIDENTIAL, jpa.getClientType());
        assertTrue(jpa.getRedirectUris().contains("http://localhost:3000/callback"));
        assertTrue(jpa.getAllowedScopes().contains("openid"));
        assertTrue(jpa.isEnabled());

        OAuthClient mappedDomain = mapper.toDomain(jpa);
        assertNotNull(mappedDomain);
        assertEquals(domain.getId(), mappedDomain.getId());
        assertEquals(domain.getClientId(), mappedDomain.getClientId());
        assertEquals(domain.getClientSecret(), mappedDomain.getClientSecret());
        assertEquals(domain.getClientName(), mappedDomain.getClientName());
        assertEquals(domain.getClientType(), mappedDomain.getClientType());
    }
}
