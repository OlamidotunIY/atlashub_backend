package com.atlashub.billing.domain.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionVolumeLedgerTest {

    @Test
    void recordTransaction_incrementsVolumeAndCount() {
        TransactionVolumeLedger ledger = TransactionVolumeLedger.create(
                1L,
                50L,
                "2026-10",
                "NGN"
        );

        assertEquals(BigDecimal.ZERO, ledger.getTotalVolume());
        assertEquals(0L, ledger.getTotalTransactionCount());

        ledger.recordTransaction(BigDecimal.valueOf(15000));
        assertEquals(BigDecimal.valueOf(15000), ledger.getTotalVolume());
        assertEquals(1L, ledger.getTotalTransactionCount());

        ledger.recordTransaction(BigDecimal.valueOf(25000));
        assertEquals(BigDecimal.valueOf(40000), ledger.getTotalVolume());
        assertEquals(2L, ledger.getTotalTransactionCount());
    }
}
