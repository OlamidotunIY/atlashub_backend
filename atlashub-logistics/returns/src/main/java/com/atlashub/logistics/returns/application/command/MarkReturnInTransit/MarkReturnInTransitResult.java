package com.atlashub.logistics.returns.application.command.MarkReturnInTransit;

public record MarkReturnInTransitResult(
        Long returnShipmentId,
        String status
) {}
