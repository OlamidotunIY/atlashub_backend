package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.SessionJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataSessionRepository extends JpaRepository<SessionJpa, Long> {
    Optional<SessionJpa> findByToken(String token);
    List<SessionJpa> findAllByUserId(String userId);
    void deleteByToken(String token);
    void deleteAllByUserId(String userId);
}
