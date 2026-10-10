package com.atlashub.accounting.gl.presentation.dto;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

public record CreateJournalEntryResponse(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status,
        int lineCount
) {}
