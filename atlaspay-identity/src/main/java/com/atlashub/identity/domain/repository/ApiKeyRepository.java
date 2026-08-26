package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.ApiKey;
import com.atlashub.identity.domain.model.KeyType;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository {
    Long nextIdentity();
    ApiKey save(ApiKey key);
    Optional<ApiKey> findById(Long id);
    Optional<ApiKey> findByKeyHash(String keyHash);
    Optional<ApiKey> findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(Long merchantId, KeyType keyType, ApiEnvironment environment);
    List<ApiKey> findAllByMerchantId(Long merchantId);
}
