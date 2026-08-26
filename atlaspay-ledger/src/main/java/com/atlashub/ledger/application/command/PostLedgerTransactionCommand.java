package com.atlashub.ledger.application.command;

import com.atlashub.ledger.domain.model.EntryType;
import com.atlashub.shared.money.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;

import com.atlashub.ledger.domain.model.SourceSystem;

public record PostLedgerTransactionCommand(
        String transactionId,
        SourceSystem sourceSystem,
        Long integrationId,
        List<EntryCommand> entries
) {
    public record EntryCommand(
            Long accountId,
            BigDecimal amount,
            CurrencyCode currency,
            EntryType type,
            String description
    ) {}
}
