package com.atlashub.accounting.gl.application.command.ApproveJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

import java.time.ZonedDateTime;

public record ApproveJournalEntryResult(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status,
        Long approvedBy,
        ZonedDateTime approvedAt
) {}
