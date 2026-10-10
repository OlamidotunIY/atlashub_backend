package com.atlashub.commerce.storefront.application.queries.GetCustomerCredit;

public record GetCustomerCreditQuery(
        Long organizationId,
        Long customerId
) {
}
