package com.atlashub.accounting.gl.application.query.GetJournalEntryById;

public record GetJournalEntryByIdQuery(
        Long entryId,
        Long organizationId
) {}
