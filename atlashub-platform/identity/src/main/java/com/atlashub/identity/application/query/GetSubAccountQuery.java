package com.atlashub.identity.application.query;


public record GetSubAccountQuery(
    Long merchantId,
    Long subAccountId
) {}
