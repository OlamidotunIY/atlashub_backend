package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateBatchRequest(
        @NotBlank String originState,
        @NotBlank String originCity,
        @NotBlank String destinationState,
        @NotBlank String destinationCity,
        List<Long> shipmentIds,
        String currency
) {}
