package com.atlashub.pay.settlement.application.commands.ResolveSettlementDispute;
import com.atlashub.shared.application.security.ApiEnvironment;
public record ResolveSettlementDisputeCommand(Long settlementId,Long organizationId,ApiEnvironment environment){}
