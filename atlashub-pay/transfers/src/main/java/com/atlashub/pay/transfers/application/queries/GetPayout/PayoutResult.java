package com.atlashub.pay.transfers.application.queries.GetPayout;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PayoutResult(
        Long id,
        Long organizationId,
        BigDecimal amount,
        String currency,
        String recipientBankCode,
        String recipientAccountNumber,
        String recipientAccountName,
        String narration,
        String status,
        String reference,
        String provider,
        String providerReference,
        String sourceSystem,
        String sourceReferenceId,
        Long initiatedBy,
        Long approvedBy,
        ZonedDateTime approvedAt,
        String failureReason,
        ZonedDateTime createdAt,
        ZonedDateTime completedAt
) {
}
