package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRiderRequest(
        @NotNull Long userId,
        @NotBlank String zoneCode,
        Long vehicleId,
        @NotBlank String licenseNumber
) {}
