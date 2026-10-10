package com.atlashub.commerce.storefront.application.commands.SendKitchenOrder;

public record KotItemDto(
        Long productId,
        String name,
        int quantity
) {
}
