package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.PayrollCostProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface PayrollCostProjectionRepository extends Repository<PayrollCostProjection> {

    Optional<PayrollCostProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth);

    List<PayrollCostProjection> findByOrgEnvAndYear(Long organizationId, String environment, String yearPrefix);
}
