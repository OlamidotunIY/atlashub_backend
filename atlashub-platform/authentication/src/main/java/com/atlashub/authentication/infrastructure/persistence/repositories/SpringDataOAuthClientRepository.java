package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.OAuthClientJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOAuthClientRepository extends JpaRepository<OAuthClientJpa, Long> {
    Optional<OAuthClientJpa> findByClientId(String clientId);
}
