package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.PayrollCostProjection;
import com.atlashub.analytics.domain.repositories.PayrollCostProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.PayrollCostProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.PayrollCostProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataPayrollCostProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PayrollCostProjectionRepositoryAdapter
        extends JpaBaseRepository<PayrollCostProjection, PayrollCostProjectionJpa>
        implements PayrollCostProjectionRepository {

    private final SpringDataPayrollCostProjectionRepository springDataRepo;

    public PayrollCostProjectionRepositoryAdapter(
            SpringDataPayrollCostProjectionRepository springDataRepo,
            PayrollCostProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "payroll_cost_projection_seq";
    }

    @Override
    public Optional<PayrollCostProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndMetricMonth(organizationId, environment, metricMonth)
                .map(mapper::toDomain);
    }

    @Override
    public List<PayrollCostProjection> findByOrgEnvAndYear(Long organizationId, String environment, String yearPrefix) {
        return springDataRepo.findByYear(organizationId, environment, yearPrefix)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
