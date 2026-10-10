package com.atlashub.pay.settlement.application.queries.GetSettlementDetails;

import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record SettlementResult(
        Long id,
        Long organizationId,
        PaymentProvider provider,
        String providerSettlementId,
        Money amount,
        ZonedDateTime settledAt,
        SettlementStatus status,
        String description
) {
}
