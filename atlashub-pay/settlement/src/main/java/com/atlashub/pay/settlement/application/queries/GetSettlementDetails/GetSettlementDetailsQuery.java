package com.atlashub.pay.settlement.application.queries.GetSettlementDetails;
import com.atlashub.shared.application.security.ApiEnvironment;

public record GetSettlementDetailsQuery(
        Long settlementId, Long organizationId, ApiEnvironment environment
) {
}
