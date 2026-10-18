package com.atlashub.analytics.application.query.GetWalletOverview;

public record GetWalletOverviewQuery(
        Long organizationId,
        String environment
) {}
