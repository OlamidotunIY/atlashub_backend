package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull Long productId,
        Long variantId,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice,
        @NotNull CurrencyCode currency,
        BigDecimal taxAmount,
        BigDecimal discountAmount
) {
}
