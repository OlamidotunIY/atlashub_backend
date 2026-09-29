package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import java.math.BigDecimal;

public record LedgerEntryResult(
        Long accountId,
        String entryType,
        BigDecimal amount
) {
}
