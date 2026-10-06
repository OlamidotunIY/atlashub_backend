package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import com.atlashub.pay.ledger.domain.valueobject.SourceSystem;

import java.time.ZonedDateTime;
import java.util.List;

public record LedgerTransactionResult(
        Long transactionId,
        String reference,
        SourceSystem sourceSystem,
        String sourceReferenceId,
        String description,
        String currency,
        ZonedDateTime postedAt,
        List<LedgerEntryResult> entries
) {
}
