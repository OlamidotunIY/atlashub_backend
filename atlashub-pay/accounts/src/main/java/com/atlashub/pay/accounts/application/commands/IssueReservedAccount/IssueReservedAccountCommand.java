package com.atlashub.pay.accounts.application.commands.IssueReservedAccount;

import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort.ReservedAccountCustomer;

public record IssueReservedAccountCommand(
        Long organizationId,
        String ownerType,
        String ownerReferenceId,
        ReservedAccountCustomer customer,
        String provider,
        String idempotencyKey,
        String apiEnvironment
) {
}
