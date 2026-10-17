package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySalesProjectionRepository extends Repository<DailySalesProjection> {

    Optional<DailySalesProjection> findByOrgEnvOutletAndDate(Long organizationId, String environment, Long outletId, LocalDate date);

    List<DailySalesProjection> findByOrgEnvAndDateRange(Long organizationId, String environment, Long outletId, LocalDate from, LocalDate to);
}
