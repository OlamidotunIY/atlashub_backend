package com.atlashub.pay.ledger.application.queries.GetPartyLedgerHistory;

import java.time.LocalDate;

public record GetPartyLedgerHistoryQuery(
        Long organizationId, String environment, String partyType, String partyReferenceId,
        String currency, LocalDate dateFrom, LocalDate dateTo, int page, int size) {}
