package com.atlashub.analytics.presentation.dto;

import java.math.BigDecimal;

public record FinancingBehaviourDto(
        Long organizationId,
        String environment,
        String paymentContextKey,
        BigDecimal totalInflow,
        BigDecimal totalOutflow,
        BigDecimal averageDailyBalance,
        BigDecimal repaymentSuccessRate,
        int anomalyCount
) {}
