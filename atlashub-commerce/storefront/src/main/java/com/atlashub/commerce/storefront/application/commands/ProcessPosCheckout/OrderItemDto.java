package com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout;

import com.atlashub.shared.domain.valueobject.Money;

public record OrderItemDto(
        Long productId,
        Long variantId,
        int quantity,
        Money unitPrice,
        Money taxAmount,
        Money discountAmount
) {
}
