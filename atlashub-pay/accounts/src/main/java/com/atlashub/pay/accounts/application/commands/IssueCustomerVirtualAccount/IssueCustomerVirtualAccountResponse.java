package com.atlashub.pay.accounts.application.commands.IssueCustomerVirtualAccount;

import com.atlashub.shared.domain.valueobject.Anchor;

public record IssueCustomerVirtualAccountResponse(
        Long virtualAccountId,
        Anchor.ReserveAccountResponse.Bank bank,
        String accountName,
        String accountNumber
) {
}
