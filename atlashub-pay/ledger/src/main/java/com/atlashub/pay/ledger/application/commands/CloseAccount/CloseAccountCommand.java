package com.atlashub.pay.ledger.application.commands.CloseAccount;

public record CloseAccountCommand(Long ledgerAccountId, Long organizationId, Long requestedByUserId) {}
