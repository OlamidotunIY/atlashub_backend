package com.atlashub.commerce.inventory.application.commands.RequestStockTransfer;

import java.util.List;

public record RequestStockTransferCommand(
        Long organizationId,
        Long sourceOutletId,
        Long destinationOutletId,
        List<TransferItemDto> items
) {
}
