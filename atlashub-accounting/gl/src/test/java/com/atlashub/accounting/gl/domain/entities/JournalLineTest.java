package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JournalLineTest {

    @Test
    @DisplayName("Should create journal line with positive amount")
    void shouldCreateJournalLine() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        JournalLine line = JournalLine.create(1L, 10L, 101L, amount, EntryType.DEBIT);

        assertEquals(1L, line.getId());
        assertEquals(10L, line.getJournalEntryId());
        assertEquals(101L, line.getAccountId());
        assertEquals(amount, line.getAmount());
        assertEquals(EntryType.DEBIT, line.getType());
    }

    @Test
    @DisplayName("Should throw InvalidEntryStateException when amount is zero or negative")
    void shouldThrowWhenAmountNotPositive() {
        Money zeroAmount = Money.zero(CurrencyCode.NGN);
        assertThrows(InvalidEntryStateException.class, () ->
                JournalLine.create(1L, 10L, 101L, zeroAmount, EntryType.DEBIT));
    }
}
