package com.atlashub.analytics.application.command.UpdatePayrollCostProjection;

import java.math.BigDecimal;

public record UpdatePayrollCostProjectionCommand(
        Long organizationId,
        String environment,
        String month,
        BigDecimal gross,
        BigDecimal deductions,
        BigDecimal net,
        int headcount
) {}
