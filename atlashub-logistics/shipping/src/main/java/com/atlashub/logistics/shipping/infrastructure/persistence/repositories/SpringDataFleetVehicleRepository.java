package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.FleetVehicleJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataFleetVehicleRepository extends JpaRepository<FleetVehicleJpa, Long> {

    Optional<FleetVehicleJpa> findByLicensePlate(String licensePlate);

    List<FleetVehicleJpa> findByOrganizationId(Long organizationId);
}
