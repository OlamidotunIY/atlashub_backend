package com.atlashub.pay.ledger.application.commands.UnfreezeAccount;

public record UnfreezeAccountCommand(
        Long ledgerAccountId,
        Long requestedByUserId,
        String restrictionType
) {}
