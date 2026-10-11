package com.atlashub.accounting.gl.application.command.PostJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.valueobject.Money;

public record PostJournalEntryResult(
        Long entryId,
        String entryNumber,
        JournalEntryStatus status,
        Money totalDebits,
        Money totalCredits
) {}
