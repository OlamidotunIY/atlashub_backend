package com.atlashub.pay.accounts.application.commands.IssueCustomerVirtualAccount;

import com.atlashub.shared.domain.valueobject.CurrencyCode;

public record IssueCustomerVirtualAccountCommand(
        Long organizationId,
        String customerId,
        String anchorCustomerId,
        CurrencyCode currency
) {
}
