package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.util.Objects;

@Getter
public class JournalLine {

    private final Long id;
    private final Long journalEntryId;
    private final Long accountId;
    private final Money amount;
    private final EntryType type;

    public JournalLine(Long id,
                       Long journalEntryId,
                       Long accountId,
                       Money amount,
                       EntryType type) {
        this.id = id;
        this.journalEntryId = journalEntryId;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
    }

    public static JournalLine create(Long id,
                                     Long journalEntryId,
                                     Long accountId,
                                     Money amount,
                                     EntryType type) {
        Objects.requireNonNull(id, "JournalLine ID must not be null");
        Objects.requireNonNull(accountId, "Account ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(type, "EntryType must not be null");
        if (!amount.isPositive()) {
            throw new InvalidEntryStateException("Journal line amount must be positive");
        }

        return new JournalLine(id, journalEntryId, accountId, amount, type);
    }
}
