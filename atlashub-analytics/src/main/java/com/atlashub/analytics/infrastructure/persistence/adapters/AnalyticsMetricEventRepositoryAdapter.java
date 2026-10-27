package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.analytics.domain.repositories.AnalyticsMetricEventRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.AnalyticsMetricEventJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.AnalyticsMetricEventMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataAnalyticsMetricEventRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnalyticsMetricEventRepositoryAdapter
        extends JpaBaseRepository<AnalyticsMetricEvent, AnalyticsMetricEventJpa>
        implements AnalyticsMetricEventRepository {

    private final SpringDataAnalyticsMetricEventRepository springDataRepo;

    public AnalyticsMetricEventRepositoryAdapter(
            SpringDataAnalyticsMetricEventRepository springDataRepo,
            AnalyticsMetricEventMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "analytics_metric_event_seq";
    }

    @Override
    public List<AnalyticsMetricEvent> findByOrgEnvAndType(Long organizationId, String environment, String metricType) {
        return springDataRepo.findByOrgEnvAndType(organizationId, environment, metricType)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsMetricEvent> findRecent(Long organizationId, String environment, int limit) {
        return springDataRepo.findRecent(organizationId, environment, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
