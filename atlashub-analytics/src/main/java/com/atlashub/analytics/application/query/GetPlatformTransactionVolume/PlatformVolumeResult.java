package com.atlashub.analytics.application.query.GetPlatformTransactionVolume;

import java.math.BigDecimal;

public record PlatformVolumeResult(
        String month,
        long totalCharges,
        BigDecimal totalVolume,
        long successfulCharges,
        long failedCharges,
        long totalPayouts,
        BigDecimal totalPayoutAmount,
        BigDecimal totalPlatformFees
) {}
