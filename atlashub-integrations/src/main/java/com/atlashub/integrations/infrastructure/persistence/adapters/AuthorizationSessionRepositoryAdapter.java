package com.atlashub.integrations.infrastructure.persistence.adapters;

import com.atlashub.integrations.domain.entities.AuthorizationSession;
import com.atlashub.integrations.domain.repositories.AuthorizationSessionRepository;
import com.atlashub.integrations.infrastructure.persistence.entities.AuthorizationSessionJpa;
import com.atlashub.integrations.infrastructure.persistence.repositories.SpringDataAuthorizationSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorizationSessionRepositoryAdapter implements AuthorizationSessionRepository {
    private final SpringDataAuthorizationSessionRepository repository;

    public AuthorizationSessionRepositoryAdapter(SpringDataAuthorizationSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthorizationSession save(AuthorizationSession session) {
        AuthorizationSessionJpa row = session.id() == null
                ? new AuthorizationSessionJpa() : repository.findById(session.id()).orElseThrow();
        row.stateHash = session.stateHash();
        row.organizationId = session.organizationId();
        row.environment = session.environment();
        row.merchantCountry = session.merchantCountry();
        row.expiresAt = session.expiresAt();
        row.consumed = session.consumed();
        return map(repository.save(row));
    }

    @Override
    public Optional<AuthorizationSession> findByStateHash(String stateHash) {
        return repository.findByStateHash(stateHash).map(this::map);
    }

    private AuthorizationSession map(AuthorizationSessionJpa row) {
        return new AuthorizationSession(row.id, row.stateHash, row.organizationId, row.environment,
                row.merchantCountry, row.expiresAt, row.consumed);
    }
}
