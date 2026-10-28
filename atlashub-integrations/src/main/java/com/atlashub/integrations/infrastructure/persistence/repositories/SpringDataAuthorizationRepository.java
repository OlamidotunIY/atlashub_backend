package com.atlashub.integrations.infrastructure.persistence.repositories;

import com.atlashub.integrations.infrastructure.persistence.entities.AuthorizationJpa;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAuthorizationRepository extends JpaRepository<AuthorizationJpa, Long> {
    Optional<AuthorizationJpa> findByConnectionIdAndEnvironment(Long connectionId, ApiEnvironment environment);

    Optional<AuthorizationJpa> findByEnvironmentAndExternalMerchantId(ApiEnvironment environment,
                                                                      String externalMerchantId);

    List<AuthorizationJpa> findByConnectionId(Long connectionId);
}
