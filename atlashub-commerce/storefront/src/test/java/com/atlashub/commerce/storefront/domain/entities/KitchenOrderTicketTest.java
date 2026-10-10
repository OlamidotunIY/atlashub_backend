package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.KitchenOrderTicketCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.KotReadyEvent;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitchenOrderTicketTest {

    @Test
    @DisplayName("Should create KOT in PENDING status and register event")
    void create_shouldInitializeAndRegisterEvent() {
        KotItem item = KotItem.create(1L, 10L, 50L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(10L, 100L, 5L, 20L, List.of(item));

        assertEquals(10L, kot.getId());
        assertEquals(100L, kot.getSalesOrderId());
        assertEquals(5L, kot.getTableId());
        assertEquals(20L, kot.getOutletId());
        assertEquals(KotStatus.PENDING, kot.getStatus());
        assertEquals(1, kot.getItems().size());
        assertEquals(1, kot.peekDomainEvents().size());
        assertTrue(kot.peekDomainEvents().getFirst() instanceof KitchenOrderTicketCreatedEvent);
    }

    @Test
    @DisplayName("Should throw exception when creating KOT with empty items")
    void create_shouldThrowException_whenItemsEmpty() {
        assertThrows(InvalidOrderStateException.class, () ->
                KitchenOrderTicket.create(10L, 100L, 5L, 20L, List.of())
        );
    }

    @Test
    @DisplayName("Should transition lifecycle: PENDING -> IN_PROGRESS -> READY -> SERVED")
    void lifecycle_shouldTransitionStatusesCorrectly() {
        KotItem item = KotItem.create(1L, 10L, 50L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(10L, 100L, 5L, 20L, List.of(item));

        kot.markInProgress();
        assertEquals(KotStatus.IN_PROGRESS, kot.getStatus());

        kot.markReady();
        assertEquals(KotStatus.READY, kot.getStatus());
        assertTrue(kot.peekDomainEvents().stream().anyMatch(e -> e instanceof KotReadyEvent));

        kot.markServed();
        assertEquals(KotStatus.SERVED, kot.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when transitioning in wrong status")
    void markReady_shouldThrowException_whenNotInProgress() {
        KotItem item = KotItem.create(1L, 10L, 50L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(10L, 100L, 5L, 20L, List.of(item));

        assertThrows(InvalidOrderStateException.class, kot::markReady);
        assertThrows(InvalidOrderStateException.class, kot::markServed);
    }
}
