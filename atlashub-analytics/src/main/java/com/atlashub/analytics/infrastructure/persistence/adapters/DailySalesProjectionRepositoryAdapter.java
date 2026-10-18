package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.DailySalesProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.DailySalesProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataDailySalesProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DailySalesProjectionRepositoryAdapter
        extends JpaBaseRepository<DailySalesProjection, DailySalesProjectionJpa>
        implements DailySalesProjectionRepository {

    private final SpringDataDailySalesProjectionRepository springDataRepo;

    public DailySalesProjectionRepositoryAdapter(
            SpringDataDailySalesProjectionRepository springDataRepo,
            DailySalesProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "daily_sales_projection_seq";
    }

    @Override
    public Optional<DailySalesProjection> findByOrgEnvOutletAndDate(Long organizationId, String environment, Long outletId, LocalDate date) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndOutletIdAndMetricDate(
                organizationId, environment, outletId, date
        ).map(mapper::toDomain);
    }

    @Override
    public List<DailySalesProjection> findByOrgEnvAndDateRange(Long organizationId, String environment, Long outletId, LocalDate from, LocalDate to) {
        return springDataRepo.findByDateRange(organizationId, environment, outletId, from, to)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
