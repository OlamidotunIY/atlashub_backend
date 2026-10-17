package com.atlashub.logistics.shipping.application.commands.UpdateShipmentLocation;

import java.math.BigDecimal;

public record UpdateShipmentLocationCommand(
        Long shipmentId,
        String locationName,
        BigDecimal latitude,
        BigDecimal longitude,
        String correlationId
) {}
