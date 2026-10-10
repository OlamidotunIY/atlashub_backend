package com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn;

public record ReturnItemDto(
        Long productId,
        int quantity
) {
}
