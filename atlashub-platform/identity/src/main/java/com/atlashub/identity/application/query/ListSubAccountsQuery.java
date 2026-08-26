package com.atlashub.identity.application.query;


public record ListSubAccountsQuery(
    Long merchantId,
    int page,
    int size
) {}
