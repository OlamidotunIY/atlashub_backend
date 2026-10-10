package com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer;

import java.util.List;

public record ReceiveStockTransferCommand(
        Long transferId,
        List<ReceivedTransferItemDto> receivedItems
) {
}
