package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.domain.valueobject.ApiEnvironment;

import java.util.List;

public interface ApiKeyRepository extends Repository<ApiKey> {
    List<ApiKey> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
}
