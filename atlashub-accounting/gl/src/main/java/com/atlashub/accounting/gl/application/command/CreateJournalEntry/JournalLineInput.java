package com.atlashub.accounting.gl.application.command.CreateJournalEntry;

import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.shared.domain.valueobject.Money;

public record JournalLineInput(
        Long accountId,
        Money amount,
        EntryType type
) {}
