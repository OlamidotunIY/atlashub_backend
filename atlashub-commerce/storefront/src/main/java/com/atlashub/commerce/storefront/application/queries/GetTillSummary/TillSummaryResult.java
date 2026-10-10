package com.atlashub.commerce.storefront.application.queries.GetTillSummary;

import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record TillSummaryResult(
        Long tillId,
        Long organizationId,
        Long outletId,
        String name,
        Money openingFloat,
        Money expectedClosingBalance,
        Money actualClosingBalance,
        TillStatus status,
        ZonedDateTime openedAt,
        ZonedDateTime closedAt,
        Long openedBy,
        Long closedBy
) {
}
