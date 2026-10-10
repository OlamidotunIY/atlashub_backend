package com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder;

public record OrderItemDto(
        Long productId,
        Long variantId,
        int quantity
) {
}
