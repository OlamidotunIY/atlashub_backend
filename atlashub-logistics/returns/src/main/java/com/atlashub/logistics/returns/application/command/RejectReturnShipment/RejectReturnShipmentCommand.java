package com.atlashub.logistics.returns.application.command.RejectReturnShipment;

public record RejectReturnShipmentCommand(
        Long organizationId,
        Long returnShipmentId,
        String reason,
        String correlationId
) {}
