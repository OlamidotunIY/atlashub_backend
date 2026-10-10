package com.atlashub.commerce.storefront.application.queries.GetSalesOrder;

import com.atlashub.shared.domain.valueobject.Money;

public record SalesOrderItemResult(
        Long id,
        Long productId,
        Long variantId,
        Integer quantity,
        Money unitPrice,
        Money totalPrice,
        Money taxAmount,
        Money discountAmount
) {
}
