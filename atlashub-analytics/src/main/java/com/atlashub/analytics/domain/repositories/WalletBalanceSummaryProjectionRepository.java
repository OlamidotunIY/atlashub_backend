package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.WalletBalanceSummaryProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface WalletBalanceSummaryProjectionRepository extends Repository<WalletBalanceSummaryProjection> {

    Optional<WalletBalanceSummaryProjection> findByOrgEnv(Long organizationId, String environment);
}
