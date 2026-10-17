package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.ProductDemandProjection;
import com.atlashub.analytics.domain.repositories.ProductDemandProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.ProductDemandProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.ProductDemandProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataProductDemandProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductDemandProjectionRepositoryAdapter
        extends JpaBaseRepository<ProductDemandProjection, ProductDemandProjectionJpa>
        implements ProductDemandProjectionRepository {

    private final SpringDataProductDemandProjectionRepository springDataRepo;

    public ProductDemandProjectionRepositoryAdapter(
            SpringDataProductDemandProjectionRepository springDataRepo,
            ProductDemandProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "product_demand_projection_seq";
    }

    @Override
    public Optional<ProductDemandProjection> findByOrgEnvProductOutletAndMonth(
            Long organizationId,
            String environment,
            Long productId,
            Long outletId,
            String month
    ) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndProductIdAndOutletIdAndMetricMonth(
                organizationId, environment, productId, outletId, month
        ).map(mapper::toDomain);
    }

    @Override
    public List<ProductDemandProjection> findTopProducts(Long organizationId, String environment, String month) {
        return springDataRepo.findTopProducts(organizationId, environment, month)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
