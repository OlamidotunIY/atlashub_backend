package com.atlashub.commerce.storefront.application.queries.ListActiveTables;

import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;

public record TableResult(
        Long id,
        Long organizationId,
        Long outletId,
        String tableNumber,
        Integer covers,
        TableStatus status,
        Long currentOrderId
) {
}
