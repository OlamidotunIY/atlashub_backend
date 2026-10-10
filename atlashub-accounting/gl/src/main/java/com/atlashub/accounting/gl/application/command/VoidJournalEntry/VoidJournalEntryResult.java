package com.atlashub.accounting.gl.application.command.VoidJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

public record VoidJournalEntryResult(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status
) {}
