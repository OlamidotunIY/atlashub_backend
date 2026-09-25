package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;
import java.util.Set;

public interface SessionRepository extends Repository<Session> {
    Optional<Session> findByToken(String token);
    Set<Session> findAllByUserId(String userId);
    void deleteByToken(String token);
    void deleteAllByUserId(String userId);
}
