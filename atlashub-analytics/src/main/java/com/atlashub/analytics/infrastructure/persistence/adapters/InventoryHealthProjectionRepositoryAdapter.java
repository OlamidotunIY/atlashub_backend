package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.InventoryHealthProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.InventoryHealthProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataInventoryHealthProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InventoryHealthProjectionRepositoryAdapter
        extends JpaBaseRepository<InventoryHealthProjection, InventoryHealthProjectionJpa>
        implements InventoryHealthProjectionRepository {

    private final SpringDataInventoryHealthProjectionRepository springDataRepo;

    public InventoryHealthProjectionRepositoryAdapter(
            SpringDataInventoryHealthProjectionRepository springDataRepo,
            InventoryHealthProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "inventory_health_projection_seq";
    }

    @Override
    public Optional<InventoryHealthProjection> findByOrgEnvOutletAndProduct(
            Long organizationId,
            String environment,
            Long outletId,
            Long productId
    ) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndOutletIdAndProductId(
                organizationId, environment, outletId, productId
        ).map(mapper::toDomain);
    }

    @Override
    public List<InventoryHealthProjection> findByOrgEnvAndOutlet(
            Long organizationId,
            String environment,
            Long outletId
    ) {
        return springDataRepo.findByOutlet(organizationId, environment, outletId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
