package com.atlashub.logistics.returns.application.command.MarkReturnInTransit;

public record MarkReturnInTransitCommand(
        Long organizationId,
        Long returnShipmentId,
        String correlationId
) {}
