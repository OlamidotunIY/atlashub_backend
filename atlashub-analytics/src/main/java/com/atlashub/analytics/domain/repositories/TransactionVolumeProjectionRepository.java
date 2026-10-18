package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TransactionVolumeProjectionRepository extends Repository<TransactionVolumeProjection> {

    Optional<TransactionVolumeProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth);

    List<TransactionVolumeProjection> findAllByMonth(String metricMonth);
}
