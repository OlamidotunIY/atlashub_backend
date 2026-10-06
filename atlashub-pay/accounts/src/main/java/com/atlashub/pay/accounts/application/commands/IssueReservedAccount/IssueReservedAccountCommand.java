package com.atlashub.pay.accounts.application.commands.IssueReservedAccount;

import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort.ReservedAccountCustomer;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;

public record IssueReservedAccountCommand(
        Long organizationId,
        ReservedAccountOwnerType ownerType,
        String ownerReferenceId,
        ReservedAccountCustomer customer,
        String provider,
        String idempotencyKey,
        String apiEnvironment
) {
}
