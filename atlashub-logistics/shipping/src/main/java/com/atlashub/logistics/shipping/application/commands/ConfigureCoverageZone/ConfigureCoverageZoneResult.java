package com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone;

public record ConfigureCoverageZoneResult(
        Long zoneId,
        String zoneCode,
        String state,
        String city,
        boolean internalFleetCovered,
        boolean partner3PLCovered,
        boolean urgentDeliverySupported,
        boolean active
) {}
