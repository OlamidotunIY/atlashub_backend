package com.atlashub.pay.accounts.application.commands.SuspendVirtualAccount;

public record SuspendVirtualAccountCommand(
        Long virtualAccountId,
        Long requestedByUserId
) {
}
