package com.atlashub.commerce.inventory.application.commands.RequestStockTransfer;

public record TransferItemDto(
        Long productId,
        int quantityRequested
) {
}
