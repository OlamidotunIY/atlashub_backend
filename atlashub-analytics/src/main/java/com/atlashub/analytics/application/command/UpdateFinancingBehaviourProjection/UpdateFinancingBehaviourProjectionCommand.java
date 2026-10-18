package com.atlashub.analytics.application.command.UpdateFinancingBehaviourProjection;

import java.math.BigDecimal;

public record UpdateFinancingBehaviourProjectionCommand(
        Long organizationId,
        String environment,
        String paymentContextKey,
        BigDecimal amount,
        boolean isInflow,
        boolean isAnomaly,
        BigDecimal repaymentRate,
        BigDecimal avgBalance
) {}
