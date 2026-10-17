package com.atlashub.analytics.application.command.UpdateWalletBalanceSummaryProjection;

import java.math.BigDecimal;

public record UpdateWalletBalanceSummaryProjectionCommand(
        Long organizationId,
        String environment,
        String ledgerType,
        BigDecimal newBalance
) {}
