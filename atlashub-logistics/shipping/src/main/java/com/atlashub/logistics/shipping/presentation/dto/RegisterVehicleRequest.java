package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterVehicleRequest(
        @NotBlank String zoneCode,
        @NotBlank String licensePlate,
        @NotNull VehicleType vehicleType,
        @NotNull BigDecimal maxWeightKg
) {}
