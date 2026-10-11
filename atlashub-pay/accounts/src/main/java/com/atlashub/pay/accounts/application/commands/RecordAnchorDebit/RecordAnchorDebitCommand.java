package com.atlashub.pay.accounts.application.commands.RecordAnchorDebit;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record RecordAnchorDebitCommand(
        String environment,
        String depositAccountId,
        String anchorTransferId,
        String transferReference,
        BigDecimal amount,
        String currency,
        String recipientName,
        String recipientAccountNumber,
        String recipientBankCode,
        String narration,
        boolean reversed,
        ZonedDateTime occurredAt
) {
}
