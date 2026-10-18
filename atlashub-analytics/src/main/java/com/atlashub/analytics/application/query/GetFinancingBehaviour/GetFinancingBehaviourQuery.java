package com.atlashub.analytics.application.query.GetFinancingBehaviour;

public record GetFinancingBehaviourQuery(
        Long organizationId,
        String environment,
        String paymentContextKey
) {}
