package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockReservationFailedEvent;
import com.atlashub.commerce.inventory.domain.events.StockReservedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockReservationTest {

    @Test
    @DisplayName("Should create stock reservation in ACTIVE status")
    void shouldCreateStockReservationInActiveStatus() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        assertEquals(10L, res.getId());
        assertEquals(20L, res.getSalesOrderId());
        assertEquals(30L, res.getOrganizationId());
        assertEquals(40L, res.getOutletId());
        assertEquals(ReservationStatus.ACTIVE, res.getStatus());
        assertNull(res.getFailureReason());
        assertEquals(1, res.getItems().size());
        assertNotNull(res.getCreatedAt());
        assertNotNull(res.getUpdatedAt());
    }

    @Test
    @DisplayName("Should confirm reservation and emit StockReservedEvent")
    void shouldConfirmReservationAndEmitEvent() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        res.confirm();

        List<DomainEvent<?>> events = res.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.getFirst() instanceof StockReservedEvent);
        StockReservedEvent event = (StockReservedEvent) events.getFirst();
        assertEquals(20L, event.payload().salesOrderId());
        assertEquals(1, event.payload().items().size());
        assertEquals(100L, event.payload().items().getFirst().productId());
        assertEquals(5, event.payload().items().getFirst().quantity());
    }

    @Test
    @DisplayName("Should throw exception when confirming empty reservation")
    void shouldThrowExceptionWhenConfirmingEmptyReservation() {
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, Collections.emptyList());

        assertThrows(InvalidInventoryOperationException.class, res::confirm);
    }

    @Test
    @DisplayName("Should fail reservation and emit StockReservationFailedEvent")
    void shouldFailReservationAndEmitEvent() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        res.fail("Insufficient inventory");

        assertEquals(ReservationStatus.FAILED, res.getStatus());
        assertEquals("Insufficient inventory", res.getFailureReason());

        List<DomainEvent<?>> events = res.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.getFirst() instanceof StockReservationFailedEvent);
        StockReservationFailedEvent event = (StockReservationFailedEvent) events.getFirst();
        assertEquals(20L, event.payload().salesOrderId());
        assertEquals("Insufficient inventory", event.payload().failureReason());
    }

    @Test
    @DisplayName("Should fulfill reservation")
    void shouldFulfillReservation() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        res.fulfill();

        assertEquals(ReservationStatus.FULFILLED, res.getStatus());
    }

    @Test
    @DisplayName("Should release reservation")
    void shouldReleaseReservation() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));

        res.release();

        assertEquals(ReservationStatus.RELEASED, res.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when mutating non-active reservation")
    void shouldThrowExceptionWhenMutatingNonActiveReservation() {
        StockReservationItem item = StockReservationItem.create(1L, 10L, 100L, 5);
        StockReservation res = StockReservation.create(10L, 20L, 30L, 40L, List.of(item));
        res.fulfill();

        assertThrows(InvalidInventoryOperationException.class, res::confirm);
        assertThrows(InvalidInventoryOperationException.class, () -> res.fail("reason"));
        assertThrows(InvalidInventoryOperationException.class, res::fulfill);
        assertThrows(InvalidInventoryOperationException.class, res::release);
    }
}
