package com.atlashub.analytics.application.query.GetPlatformFeeRevenue;

import java.math.BigDecimal;

public record PlatformFeeResult(
        String month,
        BigDecimal totalPlatformFees
) {}
