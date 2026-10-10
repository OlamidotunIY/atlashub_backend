package com.atlashub.accounting.gl.application.query.ListJournalEntries;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

public record ListJournalEntriesQuery(
        Long organizationId,
        JournalEntryStatus status
) {}
