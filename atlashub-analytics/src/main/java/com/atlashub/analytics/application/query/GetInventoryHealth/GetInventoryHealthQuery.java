package com.atlashub.analytics.application.query.GetInventoryHealth;

public record GetInventoryHealthQuery(
        Long organizationId,
        String environment,
        Long outletId
) {}
