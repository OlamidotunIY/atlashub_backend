package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.FinancialHealthProjection;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.FinancialHealthProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.FinancialHealthProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataFinancialHealthProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class FinancialHealthProjectionRepositoryAdapter
        extends JpaBaseRepository<FinancialHealthProjection, FinancialHealthProjectionJpa>
        implements FinancialHealthProjectionRepository {

    private final SpringDataFinancialHealthProjectionRepository springDataRepo;

    public FinancialHealthProjectionRepositoryAdapter(
            SpringDataFinancialHealthProjectionRepository springDataRepo,
            FinancialHealthProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "financial_health_projection_seq";
    }

    @Override
    public Optional<FinancialHealthProjection> findByOrgEnvAndPeriodEnd(Long organizationId, String environment, LocalDate periodEnd) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndPeriodEnd(organizationId, environment, periodEnd)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<FinancialHealthProjection> findLatestByOrgEnv(Long organizationId, String environment) {
        return springDataRepo.findLatest(organizationId, environment)
                .map(mapper::toDomain);
    }
}
