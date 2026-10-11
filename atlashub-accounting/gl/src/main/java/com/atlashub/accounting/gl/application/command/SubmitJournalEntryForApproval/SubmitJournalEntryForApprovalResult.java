package com.atlashub.accounting.gl.application.command.SubmitJournalEntryForApproval;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

public record SubmitJournalEntryForApprovalResult(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status
) {}
