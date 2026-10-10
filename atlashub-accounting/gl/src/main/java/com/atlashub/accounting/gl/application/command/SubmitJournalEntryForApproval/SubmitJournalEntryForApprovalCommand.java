package com.atlashub.accounting.gl.application.command.SubmitJournalEntryForApproval;

public record SubmitJournalEntryForApprovalCommand(
        Long entryId,
        Long organizationId,
        Long initiatorId
) {}
