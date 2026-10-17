package com.atlashub.analytics.application.command.UpdateOutletPerformanceProjection;

import java.math.BigDecimal;

public record UpdateOutletPerformanceProjectionCommand(
        Long organizationId,
        String environment,
        Long outletId,
        String month,
        BigDecimal sales,
        BigDecimal discounts,
        int count,
        BigDecimal expense
) {}
