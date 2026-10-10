package com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder;

public record ReceivedItemDto(Long productId, int quantityReceived) {
}
