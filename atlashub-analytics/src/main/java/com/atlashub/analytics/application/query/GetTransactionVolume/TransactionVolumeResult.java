package com.atlashub.analytics.application.query.GetTransactionVolume;

import java.math.BigDecimal;

public record TransactionVolumeResult(
        String month,
        long totalCharges,
        BigDecimal totalVolume,
        long successfulCharges,
        long failedCharges,
        long totalPayouts,
        BigDecimal totalPayoutAmount,
        BigDecimal platformFeeCollected
) {}
