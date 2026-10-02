package com.atlashub.pay.ledger.application.commands.CreateLedgerAccount;

public record CreateLedgerAccountCommand(
    Long organizationId,
    String accountType,
    Long outletId,
    String partyType,
    String partyReferenceId,
    String currency,
    String normalBalance
) {}
