package com.atlashub.billing.application.query.GetTransactionVolume;

public record GetTransactionVolumeQuery(
        Long organizationId,
        String month
) {
}
