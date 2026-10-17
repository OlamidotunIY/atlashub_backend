package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.DeliveryPerformanceProjection;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.DeliveryPerformanceProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.DeliveryPerformanceProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataDeliveryPerformanceProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DeliveryPerformanceProjectionRepositoryAdapter
        extends JpaBaseRepository<DeliveryPerformanceProjection, DeliveryPerformanceProjectionJpa>
        implements DeliveryPerformanceProjectionRepository {

    private final SpringDataDeliveryPerformanceProjectionRepository springDataRepo;

    public DeliveryPerformanceProjectionRepositoryAdapter(
            SpringDataDeliveryPerformanceProjectionRepository springDataRepo,
            DeliveryPerformanceProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "delivery_perf_projection_seq";
    }

    @Override
    public Optional<DeliveryPerformanceProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndMetricMonth(organizationId, environment, metricMonth)
                .map(mapper::toDomain);
    }
}
