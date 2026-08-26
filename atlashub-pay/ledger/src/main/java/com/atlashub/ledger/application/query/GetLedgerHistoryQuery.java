package com.atlashub.ledger.application.query;

public record GetLedgerHistoryQuery(Long integration, int page, int perPage) {}
