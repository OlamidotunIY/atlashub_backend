package com.atlashub.pay.transfers.application.queries.ListPayouts;

import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;

public record ListPayoutsQuery(
        Long organizationId,
        PayoutStatus status,
        int page,
        int size
) {
}
