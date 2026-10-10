package com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders;

import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record PurchaseOrderResult(
        Long id,
        Long organizationId,
        Long outletId,
        Long supplierId,
        PurchaseOrderStatus status,
        Money totalAmount,
        LocalDate expectedDeliveryDate,
        int itemsCount,
        ZonedDateTime createdAt
) {
}
