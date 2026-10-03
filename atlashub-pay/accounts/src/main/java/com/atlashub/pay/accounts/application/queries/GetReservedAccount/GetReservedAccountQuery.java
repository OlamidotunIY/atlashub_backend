package com.atlashub.pay.accounts.application.queries.GetReservedAccount;

public record GetReservedAccountQuery(Long organizationId, String environment, Long reservedAccountId) {
}
