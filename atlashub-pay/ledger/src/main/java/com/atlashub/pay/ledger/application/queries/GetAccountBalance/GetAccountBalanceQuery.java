package com.atlashub.pay.ledger.application.queries.GetAccountBalance;

public record GetAccountBalanceQuery(Long organizationId, String environment, String accountType) {}
