package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import java.time.ZonedDateTime;
import java.util.List;

public record LedgerTransactionResult(
        Long transactionId,
        String reference,
        String sourceSystem,
        String sourceReferenceId,
        String description,
        String currency,
        ZonedDateTime postedAt,
        List<LedgerEntryResult> entries
) {
}
