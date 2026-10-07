package com.atlashub.pay.accounts.application.queries.ListReservedAccounts;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;

public record ListReservedAccountsQuery(
        Long organizationId, String environment, ReservedAccountOwnerType ownerType, String ownerReferenceId, ExternalAccountStatus status, int page, int size) {
}
