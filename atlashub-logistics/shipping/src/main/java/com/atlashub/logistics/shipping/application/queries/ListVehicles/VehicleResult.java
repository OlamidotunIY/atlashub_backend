package com.atlashub.logistics.shipping.application.queries.ListVehicles;

import java.math.BigDecimal;

public record VehicleResult(
        Long id,
        String zoneCode,
        String licensePlate,
        String vehicleType,
        BigDecimal maxWeightKg,
        String status,
        Long currentRiderId
) {}
