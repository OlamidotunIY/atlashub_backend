package com.atlashub.pay.settlement.application.commands.DisputeSettlement;
import com.atlashub.shared.application.security.ApiEnvironment;

public record DisputeSettlementCommand(
        Long settlementId,
        Long organizationId,
        ApiEnvironment environment,
        String reason
) {
}
