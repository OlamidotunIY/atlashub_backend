package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOnlineOrderRequest(
        @NotNull Long outletId,
        @NotNull Long customerId,
        @NotEmpty List<@Valid OrderItemRequest> items,
        @NotBlank String deliveryAddress,
        @NotNull PaymentMethod paymentMethod
) {
}
