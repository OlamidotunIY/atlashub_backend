package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.TillClosedEvent;
import com.atlashub.commerce.storefront.domain.events.TillOpenedEvent;
import com.atlashub.commerce.storefront.domain.exceptions.TillNotOpenException;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TillTest {

    @Test
    @DisplayName("Should create till in OPEN status and register TillOpenedEvent")
    void create_shouldInitializeAndRegisterEvent() {
        Money initialFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(1L, 10L, 20L, "Main Counter", 500L, initialFloat);

        assertEquals(1L, till.getId());
        assertEquals(TillStatus.OPEN, till.getStatus());
        assertEquals(0, till.getOpeningFloat().amount().compareTo(new BigDecimal("10000.00")));
        assertEquals(0, till.getExpectedClosingBalance().amount().compareTo(new BigDecimal("10000.00")));
        assertEquals(500L, till.getOpenedBy());
        assertNotNull(till.getOpenedAt());
        assertEquals(1, till.peekDomainEvents().size());
        assertTrue(till.peekDomainEvents().getFirst() instanceof TillOpenedEvent);
    }

    @Test
    @DisplayName("Should record cash sale and increase expectedClosingBalance")
    void recordCashSale_shouldIncreaseExpectedBalance() {
        Money initialFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(1L, 10L, 20L, "Main Counter", 500L, initialFloat);

        till.recordCashSale(Money.of(new BigDecimal("15000.00"), CurrencyCode.NGN));

        assertEquals(0, till.getExpectedClosingBalance().amount().compareTo(new BigDecimal("25000.00")));
    }

    @Test
    @DisplayName("Should close till and register TillClosedEvent")
    void close_shouldTransitionToClosedAndRegisterEvent() {
        Money initialFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(1L, 10L, 20L, "Main Counter", 500L, initialFloat);
        till.recordCashSale(Money.of(new BigDecimal("15000.00"), CurrencyCode.NGN));

        Money actualCash = Money.of(new BigDecimal("24800.00"), CurrencyCode.NGN);
        till.close(600L, actualCash);

        assertEquals(TillStatus.CLOSED, till.getStatus());
        assertEquals(600L, till.getClosedBy());
        assertNotNull(till.getClosedAt());
        assertEquals(0, till.getActualClosingBalance().amount().compareTo(new BigDecimal("24800.00")));
        assertTrue(till.peekDomainEvents().stream().anyMatch(e -> e instanceof TillClosedEvent));
    }

    @Test
    @DisplayName("Should throw TillNotOpenException when mutating closed till")
    void closedTill_shouldThrowExceptionOnMutation() {
        Money initialFloat = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Till till = Till.create(1L, 10L, 20L, "Main Counter", 500L, initialFloat);
        till.close(600L, initialFloat);

        assertThrows(TillNotOpenException.class, () ->
                till.recordCashSale(Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN))
        );
        assertThrows(TillNotOpenException.class, () ->
                till.close(600L, initialFloat)
        );
    }
}
