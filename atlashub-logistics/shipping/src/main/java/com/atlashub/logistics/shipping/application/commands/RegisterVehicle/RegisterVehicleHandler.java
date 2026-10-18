package com.atlashub.logistics.shipping.application.commands.RegisterVehicle;

import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.logistics.shipping.domain.repositories.FleetVehicleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterVehicleHandler extends Command<RegisterVehicleCommand, Long> {

    private final FleetVehicleRepository vehicleRepository;

    public RegisterVehicleHandler(FleetVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public Long execute(RegisterVehicleCommand input) {
        Long vehicleId = vehicleRepository.nextIdentity();
        FleetVehicle vehicle = FleetVehicle.create(
                vehicleId,
                input.organizationId(),
                input.zoneCode(),
                input.licensePlate(),
                input.vehicleType(),
                input.maxWeightKg()
        );

        vehicleRepository.save(vehicle);
        return vehicle.getId();
    }
}
