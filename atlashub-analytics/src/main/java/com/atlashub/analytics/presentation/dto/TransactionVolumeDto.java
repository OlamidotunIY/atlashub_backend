package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record TransactionVolumeDto(
        String month,
        long totalCharges,
        BigDecimal totalVolume,
        long successfulCharges,
        long failedCharges,
        long totalPayouts,
        BigDecimal totalPayoutAmount,
        BigDecimal platformFeeCollected
) {}
