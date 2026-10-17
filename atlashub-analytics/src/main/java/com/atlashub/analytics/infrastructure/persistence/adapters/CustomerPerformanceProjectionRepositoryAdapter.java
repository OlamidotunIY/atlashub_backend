package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.CustomerPerformanceProjection;
import com.atlashub.analytics.domain.repositories.CustomerPerformanceProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.CustomerPerformanceProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.CustomerPerformanceProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataCustomerPerformanceProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomerPerformanceProjectionRepositoryAdapter
        extends JpaBaseRepository<CustomerPerformanceProjection, CustomerPerformanceProjectionJpa>
        implements CustomerPerformanceProjectionRepository {

    private final SpringDataCustomerPerformanceProjectionRepository springDataRepo;

    public CustomerPerformanceProjectionRepositoryAdapter(
            SpringDataCustomerPerformanceProjectionRepository springDataRepo,
            CustomerPerformanceProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "customer_perf_projection_seq";
    }

    @Override
    public Optional<CustomerPerformanceProjection> findByOrgEnvCustomerAndMonth(
            Long organizationId,
            String environment,
            Long customerId,
            String month
    ) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndCustomerIdAndMetricMonth(
                organizationId, environment, customerId, month
        ).map(mapper::toDomain);
    }

    @Override
    public List<CustomerPerformanceProjection> findByOrgEnvAndMonth(
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
