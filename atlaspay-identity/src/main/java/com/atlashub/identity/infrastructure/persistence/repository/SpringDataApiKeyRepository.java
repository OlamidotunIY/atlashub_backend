package com.atlashub.identity.infrastructure.persistence.repository;

import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.KeyType;
import com.atlashub.identity.infrastructure.persistence.entity.ApiKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpaEntity, Long> {
    Optional<ApiKeyJpaEntity> findByKeyHash(String keyHash);
    Optional<ApiKeyJpaEntity> findByIntegrationAndKeyTypeAndEnvironmentAndActiveTrue(Long merchantId, KeyType keyType, ApiEnvironment environment);
    List<ApiKeyJpaEntity> findAllByIntegration(Long merchantId);
}
