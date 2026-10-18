package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.HeadcountProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface HeadcountProjectionRepository extends Repository<HeadcountProjection> {

    Optional<HeadcountProjection> findByOrgEnv(Long organizationId, String environment);
}
