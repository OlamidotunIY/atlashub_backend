package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.logistics.shipping.domain.repositories.FleetVehicleRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.FleetVehicleJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.FleetVehicleMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataFleetVehicleRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FleetVehicleRepositoryAdapter
        extends JpaBaseRepository<FleetVehicle, FleetVehicleJpa>
        implements FleetVehicleRepository {

    private final SpringDataFleetVehicleRepository springDataRepo;

    public FleetVehicleRepositoryAdapter(
            SpringDataFleetVehicleRepository springDataRepo,
            FleetVehicleMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_vehicle_seq";
    }

    @Override
    public Optional<FleetVehicle> findByLicensePlate(String licensePlate) {
        return springDataRepo.findByLicensePlate(licensePlate).map(mapper::toDomain);
    }

    @Override
    public List<FleetVehicle> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
