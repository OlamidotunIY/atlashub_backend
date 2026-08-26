package com.atlashub.identity.application.query;


public record GetCustomerQuery(
    Long merchantId,
    Long customerId
) {}
