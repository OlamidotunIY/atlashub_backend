package com.atlashub.pay.settlement.application.commands.ConfirmSettlement;

import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

public record ConfirmSettlementCommand(
        Long organizationId,
        ApiEnvironment environment,
        Long anchorDepositAccountId,
        String anchorTransferReference,
        Money receivedAmount,
        ZonedDateTime receivedAt
) {
}
