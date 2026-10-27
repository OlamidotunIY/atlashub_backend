package com.atlashub.analytics.infrastructure.persistence.repositories;

import com.atlashub.analytics.infrastructure.persistence.entities.AnalyticsMetricEventJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SpringDataAnalyticsMetricEventRepository extends JpaRepository<AnalyticsMetricEventJpa, Long> {

    @Query("SELECT e FROM AnalyticsMetricEventJpa e WHERE e.organizationId = :orgId AND e.environment = :env " +
           "AND e.metricType = :metricType ORDER BY e.occurredAt DESC")
    List<AnalyticsMetricEventJpa> findByOrgEnvAndType(
            @Param("orgId") Long orgId,
            @Param("env") String env,
            @Param("metricType") String metricType
    );

    @Query("SELECT e FROM AnalyticsMetricEventJpa e WHERE e.organizationId = :orgId AND e.environment = :env " +
            "ORDER BY e.occurredAt DESC")
    List<AnalyticsMetricEventJpa> findRecent(@Param("orgId") Long orgId, @Param("env") String env, Pageable pageable);
}
