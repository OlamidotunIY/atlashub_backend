package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.iam.infrastructure.persistence.entities.ApiKeyJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpa, Long> {
    List<ApiKeyJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    List<ApiKeyJpa> findByOrganizationId(Long organizationId);
    java.util.Optional<ApiKeyJpa> findByPublicKey(String publicKey);
}
