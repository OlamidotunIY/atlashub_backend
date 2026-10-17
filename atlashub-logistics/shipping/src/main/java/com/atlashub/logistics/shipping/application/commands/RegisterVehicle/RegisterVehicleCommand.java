package com.atlashub.logistics.shipping.application.commands.RegisterVehicle;

import com.atlashub.logistics.shipping.domain.valueobject.VehicleType;
import java.math.BigDecimal;

public record RegisterVehicleCommand(
        Long organizationId,
        String zoneCode,
        String licensePlate,
        VehicleType vehicleType,
        BigDecimal maxWeightKg
) {}
