package com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive;

import java.time.ZonedDateTime;

public record CalculateMonthlyMerchantIncentiveCommand(
        Long organizationId,
        String billingYearMonth,
        ZonedDateTime startOfMonth,
        ZonedDateTime endOfMonth,
        String correlationId
) {}
