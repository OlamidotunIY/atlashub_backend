package com.atlashub.pay.transfers.application.queries.ListPayoutsBySource;

import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;

public record ListPayoutsBySourceQuery(
        SourceSystem sourceSystem,
        String sourceReferenceId
) {
}
