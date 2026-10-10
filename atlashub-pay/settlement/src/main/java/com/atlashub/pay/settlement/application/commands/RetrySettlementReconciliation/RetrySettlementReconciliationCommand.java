package com.atlashub.pay.settlement.application.commands.RetrySettlementReconciliation;
import com.atlashub.shared.application.security.ApiEnvironment;
public record RetrySettlementReconciliationCommand(Long settlementId,Long organizationId,ApiEnvironment environment){}
