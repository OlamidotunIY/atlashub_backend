package com.atlashub.authentication.application.port;

import com.atlashub.authentication.domain.valueobject.Session;

import java.util.Optional;
import java.util.Set;

public interface SessionPort {
    void save(Session session);
    Optional<Session> findByTokenHash(String tokenHash);
    void delete(String tokenHash);
    Set<Session> findAllByAuthAccountId(Long authAccountId);
    void deleteAllForUser(Long authAccountId);
}
