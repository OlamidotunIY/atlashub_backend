package com.atlashub.commerce.storefront.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessCreditSaleRequest(
        @NotNull Long outletId,
        @NotNull Long customerId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
