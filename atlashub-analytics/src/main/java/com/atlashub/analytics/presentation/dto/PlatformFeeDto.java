package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record PlatformFeeDto(
        String month,
        BigDecimal totalPlatformFees
) {}
