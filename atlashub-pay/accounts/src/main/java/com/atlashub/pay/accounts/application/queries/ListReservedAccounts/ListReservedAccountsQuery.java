package com.atlashub.pay.accounts.application.queries.ListReservedAccounts;

public record ListReservedAccountsQuery(
        Long organizationId, String ownerType, String ownerReferenceId, String status, int page, int size) {
}
