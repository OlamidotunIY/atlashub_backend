package com.atlashub.accounting.gl.application.command.VoidJournalEntry;

public record VoidJournalEntryCommand(
        Long entryId,
        Long organizationId,
        Long voidedBy,
        String reason
) {}
