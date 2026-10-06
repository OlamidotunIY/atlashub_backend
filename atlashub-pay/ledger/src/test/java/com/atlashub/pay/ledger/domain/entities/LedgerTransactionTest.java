package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerTransactionPostedEvent;
import com.atlashub.pay.ledger.domain.exceptions.UnbalancedLedgerTransactionException;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.pay.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LedgerTransactionTest {

    @Test
    void keeps_environment_on_transaction_and_posted_event() {
        LedgerTransaction transaction = LedgerTransaction.create(
                1L, 2L, ApiEnvironment.TEST,
                balancedEntries(), SourceSystem.CARD_CHARGE, "charge-1", "Test charge",
                CurrencyCode.NGN, ZonedDateTime.now(), "reference-1");

        assertEquals(ApiEnvironment.TEST, transaction.getEnvironment());
        LedgerTransactionPostedEvent event = (LedgerTransactionPostedEvent)
                transaction.pullDomainEvents().getFirst();
        assertEquals("TEST", event.payload().environment());
    }

    @Test
    void rejects_unbalanced_entries() {
        List<LedgerEntry> entries = List.of(
                LedgerEntry.create(1L, 1L, 10L, EntryType.DEBIT,
                        Money.of(new BigDecimal("100.00"), CurrencyCode.NGN)),
                LedgerEntry.create(2L, 1L, 11L, EntryType.CREDIT,
                        Money.of(new BigDecimal("90.00"), CurrencyCode.NGN)));

        assertThrows(UnbalancedLedgerTransactionException.class, () -> LedgerTransaction.create(
                1L, 2L, ApiEnvironment.LIVE, entries, SourceSystem.CARD_CHARGE,
                "charge-1", "Live charge", CurrencyCode.NGN, ZonedDateTime.now(), "reference-1"));
    }

    private List<LedgerEntry> balancedEntries() {
        Money amount = Money.of(new BigDecimal("100.00"), CurrencyCode.NGN);
        return List.of(
                LedgerEntry.create(1L, 1L, 10L, EntryType.DEBIT, amount),
                LedgerEntry.create(2L, 1L, 11L, EntryType.CREDIT, amount));
    }
}
