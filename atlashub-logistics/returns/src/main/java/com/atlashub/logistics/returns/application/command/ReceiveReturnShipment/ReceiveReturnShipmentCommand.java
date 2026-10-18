package com.atlashub.logistics.returns.application.command.ReceiveReturnShipment;

import java.time.ZonedDateTime;

public record ReceiveReturnShipmentCommand(
        Long organizationId,
        Long returnShipmentId,
        String inspectionNotes,
        ZonedDateTime receivedAt,
        String correlationId
) {}
