package com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone;

import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record ConfigureCoverageZoneCommand(
        String state,
        String city,
        String zoneCode,
        boolean internalFleetCovered,
        boolean partner3PLCovered,
        String preferred3PLCarrierCode,
        boolean urgentDeliverySupported,
        Money baseStandardFee,
        Money perKgStandardFee,
        BigDecimal urgentMultiplier,
        int intraStateStandardMinHours,
        int intraStateStandardMaxHours,
        int interStateStandardMinHours,
        int interStateStandardMaxHours,
        int urgentMinHours,
        int urgentMaxHours
) {}
