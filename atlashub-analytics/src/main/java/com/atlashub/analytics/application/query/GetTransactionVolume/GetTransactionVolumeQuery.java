package com.atlashub.analytics.application.query.GetTransactionVolume;

public record GetTransactionVolumeQuery(
        Long organizationId,
        String environment,
        String month
) {}
