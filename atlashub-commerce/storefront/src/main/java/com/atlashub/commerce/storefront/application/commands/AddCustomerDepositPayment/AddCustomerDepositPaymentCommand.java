package com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment;

import com.atlashub.shared.domain.valueobject.Money;

public record AddCustomerDepositPaymentCommand(
        Long depositId,
        Money paymentAmount
) {
}
