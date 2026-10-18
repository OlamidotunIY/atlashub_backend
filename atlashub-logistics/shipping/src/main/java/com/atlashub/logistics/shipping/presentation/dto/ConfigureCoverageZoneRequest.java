package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConfigureCoverageZoneRequest(
        @NotBlank String state,
        @NotBlank String city,
        @NotBlank String zoneCode,
        boolean internalFleetCovered,
        boolean partner3PLCovered,
        String preferred3PLCarrierCode,
        boolean urgentDeliverySupported,
        @NotNull Money baseStandardFee,
        @NotNull Money perKgStandardFee,
        BigDecimal urgentMultiplier,
        int intraStateStandardMinHours,
        int intraStateStandardMaxHours,
        int interStateStandardMinHours,
        int interStateStandardMaxHours,
        int urgentMinHours,
        int urgentMaxHours
) {}
