package com.atlashub.auth.domain.repository;

import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.valueobject.SessionStatus;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    Long nextIdentity();
    Session save(Session session);
    Optional<Session> findById(Long id);
    Optional<Session> findByToken(String token);
    List<Session> findByAuthAccountIdAndStatus(Long authAccountId, SessionStatus status);
    List<Session> findByAuthAccountId(Long authAccountId);
    List<Session> saveAll(List<Session> sessions);
    boolean existsByAuthAccountIdAndIpAddressAndUserAgent(Long authAccountId, String ipAddress, String userAgent);
}

