package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record PlatformVolumeDto(
        String month,
        long totalCharges,
        BigDecimal totalVolume,
        long successfulCharges,
        long failedCharges,
        long totalPayouts,
        BigDecimal totalPayoutAmount,
        BigDecimal totalPlatformFees
) {}
