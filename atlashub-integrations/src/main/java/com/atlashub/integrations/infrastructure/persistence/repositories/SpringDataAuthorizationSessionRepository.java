package com.atlashub.integrations.infrastructure.persistence.repositories;

import com.atlashub.integrations.infrastructure.persistence.entities.AuthorizationSessionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataAuthorizationSessionRepository extends JpaRepository<AuthorizationSessionJpa,Long>{
    Optional<AuthorizationSessionJpa> findByStateHash(String stateHash);
}
