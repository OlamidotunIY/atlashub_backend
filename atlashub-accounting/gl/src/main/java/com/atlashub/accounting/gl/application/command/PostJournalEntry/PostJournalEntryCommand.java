package com.atlashub.accounting.gl.application.command.PostJournalEntry;

public record PostJournalEntryCommand(
        Long entryId,
        Long organizationId
) {}
