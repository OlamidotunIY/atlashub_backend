package com.atlashub.pay.transfers.application.commands.InitiatePayout;

import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;

import java.math.BigDecimal;

public record InitiatePayoutCommand(
        Long organizationId,
        Long initiatedByUserId,
        BigDecimal amount,
        String currency,
        String recipientBankCode,
        String recipientAccountNumber,
        String recipientAccountName,
        String narration,
        String reference,
        PaymentProvider provider,
        SourceSystem sourceSystem,
        String sourceReferenceId
) {
}
