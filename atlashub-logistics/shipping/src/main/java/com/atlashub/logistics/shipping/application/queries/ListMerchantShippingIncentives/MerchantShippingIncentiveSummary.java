package com.atlashub.logistics.shipping.application.queries.ListMerchantShippingIncentives;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record MerchantShippingIncentiveSummary(
        Long incentiveId,
        String billingYearMonth,
        int eligibleShipmentCount,
        Money rebateAmount,
        String status,
        ZonedDateTime creditedAt
) {}
