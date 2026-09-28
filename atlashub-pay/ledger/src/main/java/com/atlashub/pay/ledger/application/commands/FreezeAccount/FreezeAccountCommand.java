package com.atlashub.pay.ledger.application.commands.FreezeAccount;

public record FreezeAccountCommand(Long ledgerAccountId, Long requestedByUserId) {}
