package com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit;

import com.atlashub.shared.domain.valueobject.Money;

public record CreateCustomerDepositCommand(
        Long salesOrderId,
        Money initialDeposit,
        Money totalAmount
) {
}
