package com.atlashub.pay.transfers.application.commands.InitiateBulkPayout;

import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;

import java.math.BigDecimal;
import java.util.List;

public record InitiateBulkPayoutCommand(
        Long organizationId,
        Long initiatedByUserId,
        String bulkReference,
        SourceSystem sourceSystem,
        String sourceReferenceId,
        List<BulkPayoutItem> items
) {
    public record BulkPayoutItem(
            String recipientBankCode,
            String recipientAccountNumber,
            String recipientAccountName,
            BigDecimal amount,
            String currency,
            String narration,
            String itemReference
    ) {
    }
}
