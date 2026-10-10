package com.atlashub.commerce.inventory.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record InitiateStockCountRequest(
        @NotNull(message = "Outlet ID is required")
        Long outletId
) {
}
