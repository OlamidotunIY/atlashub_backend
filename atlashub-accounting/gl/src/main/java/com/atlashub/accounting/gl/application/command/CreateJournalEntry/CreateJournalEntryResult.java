package com.atlashub.accounting.gl.application.command.CreateJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

public record CreateJournalEntryResult(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status,
        int lineCount
) {}
