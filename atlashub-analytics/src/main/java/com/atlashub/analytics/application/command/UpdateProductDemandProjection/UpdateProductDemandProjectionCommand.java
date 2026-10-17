package com.atlashub.analytics.application.command.UpdateProductDemandProjection;

import java.math.BigDecimal;

public record UpdateProductDemandProjectionCommand(
        Long organizationId,
        String environment,
        Long productId,
        Long outletId,
        String month,
        int quantity,
        BigDecimal revenue,
        boolean isRefund
) {}
