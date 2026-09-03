package com.atlashub.accounts.application.command;

import com.atlashub.shared.application.annotation.IdempotencyKey;
import com.atlashub.shared.application.usecase.Command;

public record IssueVirtualAccountCommand(
        Long integration,
        String UserCode,
        String accountName,
        String bankName,
        com.atlashub.shared.domain.money.CurrencyCode currency,
        @IdempotencyKey String idempotencyKey
) implements Command {
}
