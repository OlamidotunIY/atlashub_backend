package com.atlashub.commerce.inventory.presentation.dto;

public record StockTransferItemResponse(
        Long productId,
        int quantityRequested,
        Integer quantityReceived
) {
}
