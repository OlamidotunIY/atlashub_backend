package com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder;

import com.atlashub.shared.domain.valueobject.CurrencyCode;

import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseOrderCommand(
        Long organizationId,
        Long outletId,
        Long supplierId,
        LocalDate expectedDeliveryDate,
        CurrencyCode currency,
        List<PurchaseOrderItemDto> items
) {
}
