package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.DispatchRiderJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.DispatchRiderMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataDispatchRiderRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DispatchRiderRepositoryAdapter
        extends JpaBaseRepository<DispatchRider, DispatchRiderJpa>
        implements DispatchRiderRepository {

    private final SpringDataDispatchRiderRepository springDataRepo;

    public DispatchRiderRepositoryAdapter(
            SpringDataDispatchRiderRepository springDataRepo,
            DispatchRiderMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_rider_seq";
    }

    @Override
    public Optional<DispatchRider> findByUserId(Long userId) {
        return springDataRepo.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<DispatchRider> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<DispatchRider> findAvailableRidersByZone(String zoneCode) {
        return springDataRepo.findAvailableRidersByZone(zoneCode).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
