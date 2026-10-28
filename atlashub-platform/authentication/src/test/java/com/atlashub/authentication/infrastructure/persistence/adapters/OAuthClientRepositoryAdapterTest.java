package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.authentication.infrastructure.persistence.entities.OAuthClientJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.OAuthClientMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataOAuthClientRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuthClientRepositoryAdapterTest {

    private SpringDataOAuthClientRepository springDataRepo;
    private OAuthClientMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private OAuthClientRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataOAuthClientRepository.class);
        mapper = mock(OAuthClientMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new OAuthClientRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void finds_client_by_client_id() {
        ZonedDateTime now = ZonedDateTime.now();
        OAuthClientJpa jpa = new OAuthClientJpa(
                1L, "atlashub-web", null, "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid"),
                true, now, now, 0L
        );
        OAuthClient domain = new OAuthClient(
                1L, "atlashub-web", null, "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid"),
                true, now, now
        );

        when(springDataRepo.findByClientId("atlashub-web")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<OAuthClient> result = adapter.findByClientId("atlashub-web");

        assertTrue(result.isPresent());
        assertEquals("atlashub-web", result.get().getClientId());
    }

    @Test
    void returns_empty_for_blank_client_id() {
        assertTrue(adapter.findByClientId(null).isEmpty());
        assertTrue(adapter.findByClientId("").isEmpty());
    }
}
