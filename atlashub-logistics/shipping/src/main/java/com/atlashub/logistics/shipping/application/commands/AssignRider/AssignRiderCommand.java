package com.atlashub.logistics.shipping.application.commands.AssignRider;

public record AssignRiderCommand(
        Long shipmentId,
        Long riderId,
        Long vehicleId,
        String correlationId
) {}
