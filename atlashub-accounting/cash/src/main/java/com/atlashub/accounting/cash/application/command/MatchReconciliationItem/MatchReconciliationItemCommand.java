package com.atlashub.accounting.cash.application.command.MatchReconciliationItem;

public record MatchReconciliationItemCommand(
        Long organizationId,
        Long reconciliationId,
        Long bankLineId,
        Long journalLineId
) {}
