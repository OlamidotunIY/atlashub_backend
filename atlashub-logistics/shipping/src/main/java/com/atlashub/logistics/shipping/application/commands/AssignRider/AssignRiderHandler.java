package com.atlashub.logistics.shipping.application.commands.AssignRider;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.exceptions.RiderNotFoundException;
import com.atlashub.logistics.shipping.domain.exceptions.ShipmentNotFoundException;
import com.atlashub.logistics.shipping.domain.exceptions.VehicleNotFoundException;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.logistics.shipping.domain.repositories.FleetVehicleRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssignRiderHandler extends Command<AssignRiderCommand, Void> {

    private final ShipmentRepository shipmentRepository;
    private final DispatchRiderRepository riderRepository;
    private final FleetVehicleRepository vehicleRepository;

    public AssignRiderHandler(
            ShipmentRepository shipmentRepository,
            DispatchRiderRepository riderRepository,
            FleetVehicleRepository vehicleRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.riderRepository = riderRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public Void execute(AssignRiderCommand input) {
        Shipment shipment = shipmentRepository.findById(input.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(input.shipmentId()));

        DispatchRider rider = riderRepository.findById(input.riderId())
                .orElseThrow(() -> new RiderNotFoundException(input.riderId()));

        FleetVehicle vehicle = null;
        if (input.vehicleId() != null) {
            vehicle = vehicleRepository.findById(input.vehicleId())
                    .orElseThrow(() -> new VehicleNotFoundException(input.vehicleId()));
        }

        rider.assignDelivery(shipment.getId(), input.vehicleId());
        shipment.assignInternalRider(rider.getId(), input.vehicleId(), input.correlationId());

        if (vehicle != null) {
            vehicle.assignRider(rider.getId());
            vehicleRepository.save(vehicle);
        }

        riderRepository.save(rider);
        shipmentRepository.save(shipment);
        return null;
    }
}
