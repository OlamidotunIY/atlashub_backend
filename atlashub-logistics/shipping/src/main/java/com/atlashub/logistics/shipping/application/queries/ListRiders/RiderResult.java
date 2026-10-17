package com.atlashub.logistics.shipping.application.queries.ListRiders;

public record RiderResult(
        Long id,
        Long userId,
        String zoneCode,
        Long vehicleId,
        String licenseNumber,
        String status,
        Long currentShipmentId
) {}
