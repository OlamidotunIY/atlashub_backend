package com.atlashub.pay.accounts.application.commands.RecordAnchorFunding;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record RecordAnchorFundingCommand(
        String environment,
        String reservedAccountId,
        String depositAccountId,
        String transferReference,
        BigDecimal amount,
        String currency,
        String senderAccountName,
        String senderBankCode,
        ZonedDateTime receivedAt
) {}
