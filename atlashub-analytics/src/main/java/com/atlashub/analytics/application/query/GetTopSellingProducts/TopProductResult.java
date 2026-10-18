package com.atlashub.analytics.application.query.GetTopSellingProducts;

import java.math.BigDecimal;

public record TopProductResult(
        Long productId,
        int quantitySold,
        BigDecimal totalRevenue,
        String month
) {}
