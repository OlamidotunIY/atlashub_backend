package com.atlashub.logistics.shipping.application.commands.RegisterRider;

public record RegisterRiderCommand(
        Long organizationId,
        Long userId,
        String zoneCode,
        Long vehicleId,
        String licenseNumber
) {}
