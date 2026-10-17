package com.atlashub.analytics.application.command.UpdateDailySalesProjection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDailySalesProjectionCommand(
        Long organizationId,
        String environment,
        Long outletId,
        LocalDate date,
        BigDecimal gross,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal net,
        String paymentMethod,
        boolean isRefund
) {}
