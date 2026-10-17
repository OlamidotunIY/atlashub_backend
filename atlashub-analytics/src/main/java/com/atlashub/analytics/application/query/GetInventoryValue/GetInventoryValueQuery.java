package com.atlashub.analytics.application.query.GetInventoryValue;

public record GetInventoryValueQuery(
        Long organizationId,
        String environment,
        Long outletId
) {}
