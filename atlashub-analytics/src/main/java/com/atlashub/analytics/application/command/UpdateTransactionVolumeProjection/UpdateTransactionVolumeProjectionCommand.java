package com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection;

import java.math.BigDecimal;

public record UpdateTransactionVolumeProjectionCommand(
        Long organizationId,
        String environment,
        String month,
        boolean isCharge,
        boolean isSuccessful,
        boolean isPayout,
        BigDecimal amount,
        BigDecimal platformFee
) {}
