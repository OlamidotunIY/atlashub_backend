package com.atlashub.pay.settlement.application.queries.ListSettlements;

import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;

import java.time.ZonedDateTime;
import com.atlashub.shared.application.security.ApiEnvironment;

public record ListSettlementsQuery(
        Long organizationId,
        ApiEnvironment environment,
        SettlementStatus status,
        ZonedDateTime dateFrom,
        ZonedDateTime dateTo,
        int page,
        int size
) {
}
