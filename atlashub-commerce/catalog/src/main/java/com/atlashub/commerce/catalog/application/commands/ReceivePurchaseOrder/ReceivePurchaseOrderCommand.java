package com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder;

import java.util.List;

public record ReceivePurchaseOrderCommand(Long purchaseOrderId, List<ReceivedItemDto> receivedItems) {
}
