package com.atlashub.billing.presentation.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransactionVolumeResponse(
        Long organizationId,
        String month,
        BigDecimal totalVolume,
        Long totalTransactionCount,
        String currency,
        ZonedDateTime lastUpdatedAt
) {
}
