package com.atlashub.integrations.domain.repositories;

import com.atlashub.integrations.domain.entities.AuthorizationSession;

import java.util.Optional;

public interface AuthorizationSessionRepository {
    AuthorizationSession save(AuthorizationSession session);
    Optional<AuthorizationSession> findByStateHash(String stateHash);
}
