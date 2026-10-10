package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockCountReconciledEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.StockCountStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockCountTest {

    @Test
    @DisplayName("Should create stock count in OPEN status")
    void create_initializesAsOpen() {
        StockCount sc = StockCount.create(1L, 10L, 20L);

        assertEquals(1L, sc.getId());
        assertEquals(10L, sc.getOrganizationId());
        assertEquals(20L, sc.getOutletId());
        assertEquals(StockCountStatus.OPEN, sc.getStatus());
        assertTrue(sc.getItems().isEmpty());
        assertNotNull(sc.getStartedAt());
        assertNull(sc.getReconciledAt());
    }

    @Test
    @DisplayName("Should add items and update counted quantity")
    void addItem_andRecordCount_updatesCorrectly() {
        StockCount sc = StockCount.create(1L, 10L, 20L);
        sc.addItem(100L, 200L, 300L, 50);

        assertEquals(1, sc.getItems().size());
        StockCountItem item = sc.getItems().get(0);
        assertEquals(100L, item.getId());
        assertEquals(200L, item.getInventoryId());
        assertEquals(300L, item.getProductId());
        assertEquals(50, item.getSystemQty());
        assertNull(item.getCountedQty());

        sc.updateCount(200L, 48);
        assertEquals(48, item.getCountedQty());
    }

    @Test
    @DisplayName("Should reconcile stock count and register event")
    void reconcile_transitionsToReconciledAndEmitsEvent() {
        StockCount sc = StockCount.create(1L, 10L, 20L);
        sc.addItem(100L, 200L, 300L, 50);
        sc.updateCount(200L, 48);

        sc.reconcile();

        assertEquals(StockCountStatus.RECONCILED, sc.getStatus());
        assertNotNull(sc.getReconciledAt());
        List<DomainEvent<?>> events = sc.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof StockCountReconciledEvent);
    }

    @Test
    @DisplayName("Should throw exception when reconciling already reconciled stock count")
    void reconcile_whenAlreadyReconciled_throwsException() {
        StockCount sc = StockCount.create(1L, 10L, 20L);
        sc.reconcile();

        assertThrows(InvalidInventoryOperationException.class, sc::reconcile);
    }

    @Test
    @DisplayName("Should throw exception when adding items or updating count in reconciled count")
    void mutate_whenReconciled_throwsException() {
        StockCount sc = StockCount.create(1L, 10L, 20L);
        sc.addItem(100L, 200L, 300L, 50);
        sc.reconcile();

        assertThrows(InvalidInventoryOperationException.class, () ->
                sc.addItem(101L, 201L, 301L, 10)
        );
        assertThrows(InvalidInventoryOperationException.class, () ->
                sc.updateCount(200L, 45)
        );
    }
}
