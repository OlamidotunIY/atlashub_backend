package com.atlashub.pay.ledger.application.commands.PostLedgerTransaction;

import java.util.List;

public record PostLedgerTransactionCommand(
        Long organizationId,
        String reference,
        String sourceSystem,
        String sourceReferenceId,
        String description,
        String currency,
        List<LedgerEntryRequest> entries
) {
    public record LedgerEntryRequest(
            Long accountId,
            String entryType,
            java.math.BigDecimal amount
    ) {}
}
