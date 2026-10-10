package com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer;

public record ReceivedTransferItemDto(
        Long productId,
        int quantityReceived
) {
}
