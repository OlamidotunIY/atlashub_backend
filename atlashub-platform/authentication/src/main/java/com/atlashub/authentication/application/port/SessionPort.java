package com.atlashub.authentication.application.port;

import com.atlashub.authentication.domain.valueobject.Session;
import java.util.Optional;

public interface SessionPort {
    void save(Session token);
    Optional<Session> findByTokenHash(String tokenHash);
    void delete(String tokenHash);
    void deleteAllForUser(Long userId);
}
