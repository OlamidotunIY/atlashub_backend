package com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent;

import java.math.BigDecimal;

public record ProcessLedgerEventCommand(
        Action action,
        Long organizationId,
        String environment,
        Long outletId,
        String partyType,
        String partyReferenceId,
        String reference,
        String sourceSystem,
        String sourceReferenceId,
        BigDecimal amount,
        String currency
) {
    public enum Action {
        BOOTSTRAP_ORGANIZATION,
        CREATE_PARTY_ACCOUNT,
        CREATE_TILL_ACCOUNT,
        CHARGE_RECEIVED,
        PROVIDER_SETTLED,
        RESERVED_ACCOUNT_FUNDED,
        ORGANIZATION_ACCOUNT_FUNDED,
        PAYOUT_COMPLETED,
        TILL_OPENED,
        TILL_CLOSED,
        RESTRICT_ORGANIZATION,
        RELEASE_ORGANIZATION
    }
}
