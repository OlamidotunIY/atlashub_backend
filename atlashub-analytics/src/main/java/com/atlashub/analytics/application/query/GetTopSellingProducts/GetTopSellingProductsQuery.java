package com.atlashub.analytics.application.query.GetTopSellingProducts;

public record GetTopSellingProductsQuery(
        Long organizationId,
        String environment,
        String month,
        int limit
) {}
