package com.atlashub.commerce.storefront.application.queries.GetCustomerCredit;

import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
import com.atlashub.shared.domain.valueobject.Money;

public record CustomerCreditResult(
        Long id,
        Long organizationId,
        Long customerId,
        Money creditLimit,
        Money outstandingDebt,
        CreditStatus status
) {
}
