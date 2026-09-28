package com.atlashub.pay.ledger.application.queries.GetLedgerHistory;

import java.time.LocalDate;

public record GetLedgerHistoryQuery(
        Long organizationId,
        Long accountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
) {
}
