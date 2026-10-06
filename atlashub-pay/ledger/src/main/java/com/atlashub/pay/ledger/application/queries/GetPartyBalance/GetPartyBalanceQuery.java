package com.atlashub.pay.ledger.application.queries.GetPartyBalance;

public record GetPartyBalanceQuery(
        Long organizationId, String environment, String partyType, String partyReferenceId, String currency) {}
