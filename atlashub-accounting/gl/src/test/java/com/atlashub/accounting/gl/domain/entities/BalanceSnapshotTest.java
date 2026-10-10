package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BalanceSnapshotTest {

    @Test
    @DisplayName("Should create balance snapshot")
    void shouldCreateBalanceSnapshot() {
        Money balance = Money.of(new BigDecimal("250000.00"), CurrencyCode.NGN);
        BalanceSnapshot snapshot = BalanceSnapshot.create(
                201L,
                101L,
                LocalDate.of(2026, 9, 30),
                balance
        );

        assertEquals(201L, snapshot.getId());
        assertEquals(101L, snapshot.getAccountId());
        assertEquals(LocalDate.of(2026, 9, 30), snapshot.getSnapshotDate());
        assertEquals(balance, snapshot.getBalance());
        assertNotNull(snapshot.getCreatedAt());
    }
}
