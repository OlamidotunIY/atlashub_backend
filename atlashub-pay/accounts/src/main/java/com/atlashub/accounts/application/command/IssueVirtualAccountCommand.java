package com.atlashub.accounts.application.command;

import com.atlashub.shared.annotation.IdempotencyKey;
import com.atlashub.shared.usecase.Command;

public record IssueVirtualAccountCommand(
        Long integration,
        String UserCode,
        String accountName,
        String bankName,
        com.atlashub.shared.money.CurrencyCode currency,
        @IdempotencyKey String idempotencyKey
) implements Command {
}
