package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessPosCheckoutRequest(
        @NotNull Long outletId,
        Long tillId,
        Long customerId,
        @NotNull OrderType type,
        @NotEmpty List<@Valid OrderItemRequest> items,
        Long discountId,
        @NotNull PaymentMethod paymentMethod
) {
}
