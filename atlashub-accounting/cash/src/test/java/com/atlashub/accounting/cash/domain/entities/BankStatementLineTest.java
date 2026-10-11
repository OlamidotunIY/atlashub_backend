package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankStatementLineTest {

    @Test
    @DisplayName("Should create statement line successfully with valid fields")
    void create_valid_success() {
        Money amount = Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN);
        LocalDate date = LocalDate.of(2026, 10, 10);

        BankStatementLine line = BankStatementLine.create(1L, 100L, date, "Transfer In", amount, StatementEntryType.CREDIT);

        assertEquals(1L, line.getId());
        assertEquals(100L, line.getReconciliationId());
        assertEquals(date, line.getTransactionDate());
        assertEquals("Transfer In", line.getDescription());
        assertEquals(amount, line.getAmount());
        assertEquals(StatementEntryType.CREDIT, line.getType());
    }

    @Test
    @DisplayName("Should throw when amount is zero or negative")
    void create_zeroAmount_throwsException() {
        Money zero = Money.of(BigDecimal.ZERO, CurrencyCode.NGN);

        assertThrows(InvalidReconciliationStateException.class, () ->
                BankStatementLine.create(1L, 100L, LocalDate.now(), "Desc", zero, StatementEntryType.DEBIT));
    }

    @Test
    @DisplayName("Should throw when type is null")
    void create_nullType_throwsException() {
        Money amount = Money.of(BigDecimal.TEN, CurrencyCode.NGN);

        assertThrows(InvalidReconciliationStateException.class, () ->
                BankStatementLine.create(1L, 100L, LocalDate.now(), "Desc", amount, null));
    }
}
