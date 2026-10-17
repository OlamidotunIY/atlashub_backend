package com.atlashub.analytics.application.query.GetFinancingBehaviour;

import java.math.BigDecimal;

public record FinancingBehaviourResult(
        Long organizationId,
        String environment,
        String paymentContextKey,
        BigDecimal totalInflow,
        BigDecimal totalOutflow,
        BigDecimal averageDailyBalance,
        BigDecimal repaymentSuccessRate,
        int anomalyCount
) {}
