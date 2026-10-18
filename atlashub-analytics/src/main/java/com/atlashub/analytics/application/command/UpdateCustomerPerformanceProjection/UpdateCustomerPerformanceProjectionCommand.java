package com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection;

import java.math.BigDecimal;

public record UpdateCustomerPerformanceProjectionCommand(
        Long organizationId,
        String environment,
        Long customerId,
        String month,
        BigDecimal amount,
        boolean isRefund
) {}
