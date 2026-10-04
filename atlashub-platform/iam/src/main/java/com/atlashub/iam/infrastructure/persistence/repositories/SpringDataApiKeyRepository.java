package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.iam.infrastructure.persistence.entities.ApiKeyJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpa, Long> {
    List<ApiKeyJpa> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
    List<ApiKeyJpa> findByOrganizationId(Long organizationId);
    java.util.Optional<ApiKeyJpa> findByPublicKey(String publicKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select apiKey from ApiKeyJpa apiKey where apiKey.id = :id and apiKey.organizationId = :organizationId")
    Optional<ApiKeyJpa> findByIdAndOrganizationIdForUpdate(
            @Param("id") Long id, @Param("organizationId") Long organizationId);
}
