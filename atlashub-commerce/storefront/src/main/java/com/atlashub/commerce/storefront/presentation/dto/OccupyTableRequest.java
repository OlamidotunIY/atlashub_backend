package com.atlashub.commerce.storefront.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OccupyTableRequest(
        @NotNull Long salesOrderId,
        @Min(1) int covers
) {
}
