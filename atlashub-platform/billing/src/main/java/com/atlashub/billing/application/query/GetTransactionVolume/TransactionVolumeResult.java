package com.atlashub.billing.application.query.GetTransactionVolume;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransactionVolumeResult(
        Long organizationId,
        String month,
        BigDecimal totalVolume,
        Long totalTransactionCount,
        String currency,
        ZonedDateTime lastUpdatedAt
) {
}
