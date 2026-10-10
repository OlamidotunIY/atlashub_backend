package com.atlashub.commerce.storefront.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SendKitchenOrderRequest(
        @NotNull Long salesOrderId,
        @NotNull Long tableId,
        @NotNull Long outletId,
        @NotEmpty List<@Valid KotItemRequest> items
) {
}
