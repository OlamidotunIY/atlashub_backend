package com.atlashub.accounting.gl.application.command.ApproveJournalEntry;

public record ApproveJournalEntryCommand(
        Long entryId,
        Long organizationId,
        Long approverId
) {}
