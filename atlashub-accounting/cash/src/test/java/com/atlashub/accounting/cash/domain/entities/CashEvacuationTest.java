package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.events.CashEvacuatedEvent;
import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashEvacuationTest {

    @Test
    @DisplayName("Should create pending cash evacuation successfully")
    void create_valid_pendingEvacuation() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);

        CashEvacuation evacuation = CashEvacuation.create(1L, 10L, 20L, 30L, amount, 99L);

        assertEquals(1L, evacuation.getId());
        assertEquals(10L, evacuation.getOrganizationId());
        assertEquals(20L, evacuation.getOutletId());
        assertEquals(30L, evacuation.getTillId());
        assertEquals(amount, evacuation.getAmount());
        assertEquals(99L, evacuation.getEvacuatedBy());
        assertEquals(EvacuationStatus.PENDING, evacuation.getStatus());
        assertNotNull(evacuation.getEvacuatedAt());
    }

    @Test
    @DisplayName("Should throw when cash evacuation amount is zero or negative")
    void create_nonPositiveAmount_throwsException() {
        Money zeroAmount = Money.of(BigDecimal.ZERO, CurrencyCode.NGN);

        assertThrows(InvalidReconciliationStateException.class, () ->
                CashEvacuation.create(1L, 10L, 20L, 30L, zeroAmount, 99L));
    }

    @Test
    @DisplayName("Should confirm pending evacuation and register CashEvacuatedEvent")
    void confirm_pendingEvacuation_success() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CashEvacuation evacuation = CashEvacuation.create(1L, 10L, 20L, 30L, amount, 99L);

        evacuation.confirm();

        assertEquals(EvacuationStatus.CONFIRMED, evacuation.getStatus());
        assertNotNull(evacuation.getConfirmedAt());

        List<DomainEvent<?>> events = evacuation.pullDomainEvents();
        assertFalse(events.isEmpty());
        assertTrue(events.get(0) instanceof CashEvacuatedEvent);
        CashEvacuatedEvent event = (CashEvacuatedEvent) events.get(0);
        assertEquals(1L, event.payload().evacuationId());
        assertEquals(new BigDecimal("50000.00"), event.payload().amount());
        assertEquals("NGN", event.payload().currency());
    }

    @Test
    @DisplayName("Should throw when confirming already confirmed evacuation")
    void confirm_alreadyConfirmed_throwsException() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CashEvacuation evacuation = CashEvacuation.create(1L, 10L, 20L, 30L, amount, 99L);

        evacuation.confirm();

        assertThrows(InvalidReconciliationStateException.class, evacuation::confirm);
    }
}
