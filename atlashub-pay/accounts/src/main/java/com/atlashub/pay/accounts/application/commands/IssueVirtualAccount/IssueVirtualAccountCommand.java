package com.atlashub.pay.accounts.application.commands.IssueVirtualAccount;

import com.atlashub.shared.domain.valueobject.CurrencyCode;

public record IssueVirtualAccountCommand(
        Long organizationId,
        String anchorCustomerId,
        CurrencyCode currency
) {
}
