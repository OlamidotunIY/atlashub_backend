package com.atlashub.accounting.gl.application.query.GetJournalEntryById;

import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.shared.domain.valueobject.Money;

public record JournalLineResult(
        Long id,
        Long accountId,
        Money amount,
        EntryType type
) {}
