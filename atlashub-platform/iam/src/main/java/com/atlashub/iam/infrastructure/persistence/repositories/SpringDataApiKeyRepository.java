package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import com.atlashub.iam.infrastructure.persistence.entities.ApiKeyJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpa, Long> {
    List<ApiKeyJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
}
