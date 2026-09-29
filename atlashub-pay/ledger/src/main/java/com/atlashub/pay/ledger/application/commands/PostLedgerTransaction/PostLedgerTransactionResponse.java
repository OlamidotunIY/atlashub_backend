package com.atlashub.pay.ledger.application.commands.PostLedgerTransaction;

import java.time.ZonedDateTime;

public record PostLedgerTransactionResponse(
        Long transactionId,
        String reference,
        ZonedDateTime postedAt
) {}
