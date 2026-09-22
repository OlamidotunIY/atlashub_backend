package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.AuthAccountJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAuthAccountRepository extends JpaRepository<AuthAccountJpa, Long> {
    Optional<AuthAccountJpa> findByEmail(String email);
    Optional<AuthAccountJpa> findByUserId(Long userId);
}
