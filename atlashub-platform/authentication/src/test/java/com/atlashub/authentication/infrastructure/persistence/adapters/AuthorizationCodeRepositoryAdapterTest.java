package com.atlashub.authentication.infrastructure.persistence.adapters;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.authentication.infrastructure.persistence.entities.AuthorizationCodeJpa;
import com.atlashub.authentication.infrastructure.persistence.mappers.AuthorizationCodeMapper;
import com.atlashub.authentication.infrastructure.persistence.repositories.SpringDataAuthorizationCodeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationCodeRepositoryAdapterTest {

    private SpringDataAuthorizationCodeRepository springDataRepo;
    private AuthorizationCodeMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private AuthorizationCodeRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataAuthorizationCodeRepository.class);
        mapper = mock(AuthorizationCodeMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new AuthorizationCodeRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void finds_authorization_code_by_hash() {
        ZonedDateTime now = ZonedDateTime.now();
        AuthorizationCodeJpa jpa = new AuthorizationCodeJpa(
                10L, "hash_xyz", "atlashub-web", 42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge", CodeChallengeMethod.S256, null,
                now.plusMinutes(5), false, now, now, 0L
        );
        AuthorizationCode domain = new AuthorizationCode(
                10L, "hash_xyz", "atlashub-web", 42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge", CodeChallengeMethod.S256, null,
                now.plusMinutes(5), false, now, now
        );

        when(springDataRepo.findByCodeHash("hash_xyz")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<AuthorizationCode> result = adapter.findByCodeHash("hash_xyz");

        assertTrue(result.isPresent());
        assertEquals("hash_xyz", result.get().getCodeHash());
    }

    @Test
    void deletes_expired_codes() {
        ZonedDateTime threshold = ZonedDateTime.now();
        adapter.deleteExpiredBefore(threshold);
        verify(springDataRepo).deleteExpiredBefore(threshold);
    }
}
