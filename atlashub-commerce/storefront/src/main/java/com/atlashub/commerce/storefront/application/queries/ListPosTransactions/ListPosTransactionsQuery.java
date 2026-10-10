package com.atlashub.commerce.storefront.application.queries.ListPosTransactions;

import java.time.ZonedDateTime;

public record ListPosTransactionsQuery(
        Long outletId,
        Long tillId,
        Long cashierId,
        ZonedDateTime from,
        ZonedDateTime to,
        int page,
        int size
) {
}
