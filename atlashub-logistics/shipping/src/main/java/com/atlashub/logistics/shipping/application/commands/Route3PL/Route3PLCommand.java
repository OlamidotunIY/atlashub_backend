package com.atlashub.logistics.shipping.application.commands.Route3PL;

public record Route3PLCommand(
        Long shipmentId,
        String carrierCode,
        String correlationId
) {}
