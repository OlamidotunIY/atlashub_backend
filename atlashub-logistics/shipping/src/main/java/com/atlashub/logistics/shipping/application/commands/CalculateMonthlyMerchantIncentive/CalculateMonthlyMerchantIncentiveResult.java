package com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive;

import com.atlashub.shared.domain.valueobject.Money;

public record CalculateMonthlyMerchantIncentiveResult(
        Long incentiveId,
        Long organizationId,
        String billingYearMonth,
        int eligibleShipmentCount,
        Money rebateAmount,
        String status
) {}
