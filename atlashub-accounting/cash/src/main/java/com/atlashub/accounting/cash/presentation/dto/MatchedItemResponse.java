package com.atlashub.accounting.cash.presentation.dto;

public record MatchedItemResponse(
        Long id,
        Long reconciliationId,
        Long bankStatementLineId,
        Long journalLineId
) {}
