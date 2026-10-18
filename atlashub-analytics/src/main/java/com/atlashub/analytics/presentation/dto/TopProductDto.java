package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record TopProductDto(
        Long productId,
        int quantitySold,
        BigDecimal totalRevenue,
        String month
) {}
