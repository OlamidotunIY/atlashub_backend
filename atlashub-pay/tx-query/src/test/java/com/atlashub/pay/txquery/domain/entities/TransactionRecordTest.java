package com.atlashub.pay.txquery.domain.entities;

import com.atlashub.pay.txquery.domain.valueobject.*;import com.atlashub.shared.application.security.ApiEnvironment;import com.atlashub.shared.domain.valueobject.*;import org.junit.jupiter.api.Test;import java.math.BigDecimal;import java.time.ZonedDateTime;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class TransactionRecordTest {
    @Test
    void createsAndAdvancesLifecycle() {
        var record = record(TransactionType.CHARGE);
        assertEquals(TransactionStatus.PENDING, record.getStatus());
        record.updateStatus(TransactionStatus.SUCCESSFUL, ZonedDateTime.now(), null);
        assertEquals(TransactionStatus.SUCCESSFUL, record.getStatus());
        assertNotNull(record.getCompletedAt());
    }

    @Test
    void attachesAccountEntries() {
        var record = record(TransactionType.LEDGER_POST);
        record.attachAccountEntries(List.of(new TransactionAccountEntry(3L, 1L, 4L, "OPERATING", "Operating",
                "CREDIT", TransactionDirection.INCOMING, Money.of(BigDecimal.TEN, CurrencyCode.NGN))));
        assertEquals(TransactionDirection.INCOMING, record.primaryDirection());
    }

    @Test
    void uses_business_semantics_for_organization_wide_direction() {
        assertEquals(TransactionDirection.INCOMING, record(TransactionType.ACCOUNT_FUNDING).primaryDirection());
        assertEquals(TransactionDirection.OUTGOING, record(TransactionType.PAYOUT).primaryDirection());
        assertEquals(TransactionDirection.INTERNAL, record(TransactionType.SETTLEMENT).primaryDirection());
    }

    private TransactionRecord record(TransactionType type) {
        return TransactionRecord.create(1L, 2L, ApiEnvironment.TEST, type, TransactionStatus.PENDING,
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), "CARD", "PAYSTACK", "ref", "SYSTEM",
                "source-1", null, null, null, null, null, "Description", Map.of(), ZonedDateTime.now());
    }
}
