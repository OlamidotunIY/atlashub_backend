package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface FleetVehicleRepository extends Repository<FleetVehicle> {

    Optional<FleetVehicle> findByLicensePlate(String licensePlate);

    List<FleetVehicle> findByOrganizationId(Long organizationId);
}
