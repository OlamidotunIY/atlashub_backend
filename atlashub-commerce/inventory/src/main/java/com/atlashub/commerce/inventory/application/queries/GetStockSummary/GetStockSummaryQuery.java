package com.atlashub.commerce.inventory.application.queries.GetStockSummary;

public record GetStockSummaryQuery(
        Long organizationId,
        Long outletId
) {
}
