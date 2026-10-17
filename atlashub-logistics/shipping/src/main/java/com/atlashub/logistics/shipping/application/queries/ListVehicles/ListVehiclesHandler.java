package com.atlashub.logistics.shipping.application.queries.ListVehicles;

import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.logistics.shipping.domain.repositories.FleetVehicleRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListVehiclesHandler extends Query<ListVehiclesQuery, List<VehicleResult>> {

    private final FleetVehicleRepository vehicleRepository;

    public ListVehiclesHandler(FleetVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResult> execute(ListVehiclesQuery input) {
        List<FleetVehicle> vehicles = vehicleRepository.findByOrganizationId(input.organizationId());

        return vehicles.stream()
                .map(v -> new VehicleResult(
                        v.getId(),
                        v.getZoneCode(),
                        v.getLicensePlate(),
                        v.getVehicleType().name(),
                        v.getMaxWeightKg(),
                        v.getStatus().name(),
                        v.getCurrentRiderId()
                ))
                .toList();
    }
}
