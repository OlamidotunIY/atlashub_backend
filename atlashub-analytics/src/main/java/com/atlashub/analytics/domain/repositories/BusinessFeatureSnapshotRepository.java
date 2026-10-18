package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.BusinessFeatureSnapshot;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface BusinessFeatureSnapshotRepository extends Repository<BusinessFeatureSnapshot> {

    Optional<BusinessFeatureSnapshot> findLatestByOrgEnv(Long organizationId, String environment, String schemaVersion);
}
