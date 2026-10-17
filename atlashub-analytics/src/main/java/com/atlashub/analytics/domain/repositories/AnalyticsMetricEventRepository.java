package com.atlashub.analytics.domain.repositories;

import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface AnalyticsMetricEventRepository extends Repository<AnalyticsMetricEvent> {

    List<AnalyticsMetricEvent> findByOrgEnvAndType(Long organizationId, String environment, String metricType);
}
