package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import com.atlashub.pay.ledger.domain.valueobject.EntryType;

import java.math.BigDecimal;

public record LedgerEntryResult(
        Long accountId,
        EntryType entryType,
        BigDecimal amount
) {
}
