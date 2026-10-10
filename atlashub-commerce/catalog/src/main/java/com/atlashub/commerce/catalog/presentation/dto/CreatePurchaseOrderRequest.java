package com.atlashub.commerce.catalog.presentation.dto;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderRequest(
        @NotNull(message = "Outlet ID is required")
        Long outletId,
        @NotNull(message = "Supplier ID is required")
        Long supplierId,
        LocalDate expectedDeliveryDate,
        CurrencyCode currency,
        @Valid
        List<PurchaseOrderItemRequest> items
) {
}
