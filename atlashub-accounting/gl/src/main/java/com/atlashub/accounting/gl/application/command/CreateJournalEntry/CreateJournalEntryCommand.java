package com.atlashub.accounting.gl.application.command.CreateJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.EntrySource;

import java.time.LocalDate;
import java.util.List;

public record CreateJournalEntryCommand(
        Long organizationId,
        String entryNumber,
        LocalDate date,
        String reference,
        String description,
        EntrySource source,
        Long initiatedBy,
        List<JournalLineInput> lines
) {}
