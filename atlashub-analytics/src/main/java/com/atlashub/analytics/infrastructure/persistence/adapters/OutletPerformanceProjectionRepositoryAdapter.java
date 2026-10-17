package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.OutletPerformanceProjection;
import com.atlashub.analytics.domain.repositories.OutletPerformanceProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.OutletPerformanceProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.OutletPerformanceProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataOutletPerformanceProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OutletPerformanceProjectionRepositoryAdapter
        extends JpaBaseRepository<OutletPerformanceProjection, OutletPerformanceProjectionJpa>
        implements OutletPerformanceProjectionRepository {

    private final SpringDataOutletPerformanceProjectionRepository springDataRepo;

    public OutletPerformanceProjectionRepositoryAdapter(
            SpringDataOutletPerformanceProjectionRepository springDataRepo,
            OutletPerformanceProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "outlet_perf_projection_seq";
    }

    @Override
    public Optional<OutletPerformanceProjection> findByOrgEnvOutletAndMonth(
            Long organizationId,
            String environment,
            Long outletId,
            String month
    ) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndOutletIdAndMetricMonth(
                organizationId, environment, outletId, month
        ).map(mapper::toDomain);
    }

    @Override
    public List<OutletPerformanceProjection> findByOrgEnvAndMonth(
            Long organizationId,
            String environment,
            String month
    ) {
        return springDataRepo.findByMonth(organizationId, environment, month)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
