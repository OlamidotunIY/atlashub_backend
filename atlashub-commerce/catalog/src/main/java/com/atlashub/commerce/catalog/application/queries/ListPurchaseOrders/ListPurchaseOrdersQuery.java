package com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders;

import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;

public record ListPurchaseOrdersQuery(
        Long organizationId,
        PurchaseOrderStatus status,
        int page,
        int size
) {
}
