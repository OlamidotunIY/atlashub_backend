package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRiderRequest(
        @NotNull Long riderId,
        Long vehicleId
) {}
