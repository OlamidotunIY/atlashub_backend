package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.WarehouseStockAdjustedEvent;
import com.atlashub.logistics.warehousing.domain.exceptions.InsufficientWarehouseStockException;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseInventoryTest {

    @Test
    @DisplayName("Should create inventory with initial stock and register event")
    void shouldCreateInventory() {
        WarehouseInventory inventory = WarehouseInventory.create(
                10L,
                100L,
                1L,
                501L,
                100,
                "corr-inv-1"
        );

        assertNotNull(inventory);
        assertEquals(100, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(100, inventory.availableQuantity());

        assertFalse(inventory.peekDomainEvents().isEmpty());
        assertTrue(inventory.peekDomainEvents().get(0) instanceof WarehouseStockAdjustedEvent);
    }

    @Test
    @DisplayName("Should reserve stock when available and throw when insufficient")
    void shouldReserveStock() {
        WarehouseInventory inventory = WarehouseInventory.create(
                10L,
                100L,
                1L,
                501L,
                50,
                "corr-inv-2"
        );

        inventory.reserveStock(20, "corr-res-1");
        assertEquals(50, inventory.getQuantity());
        assertEquals(20, inventory.getReservedQuantity());
        assertEquals(30, inventory.availableQuantity());

        // Attempt to reserve 35 (only 30 available)
        assertThrows(InsufficientWarehouseStockException.class, () ->
                inventory.reserveStock(35, "corr-res-2")
        );
    }

    @Test
    @DisplayName("Should deduct reserved stock upon dispatch")
    void shouldDeductStock() {
        WarehouseInventory inventory = WarehouseInventory.create(
                10L,
                100L,
                1L,
                501L,
                50,
                "corr-inv-3"
        );

        inventory.reserveStock(20, "corr-res-1");
        inventory.deductStock(20, "corr-ded-1");

        assertEquals(30, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(30, inventory.availableQuantity());
    }

    @Test
    @DisplayName("Should release reserved stock when order or transfer is cancelled")
    void shouldReleaseReservedStock() {
        WarehouseInventory inventory = WarehouseInventory.create(
                10L,
                100L,
                1L,
                501L,
                50,
                "corr-inv-4"
        );

        inventory.reserveStock(15, "corr-res-1");
        inventory.releaseReservedStock(15, "corr-rel-1");

        assertEquals(50, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(50, inventory.availableQuantity());
    }

    @Test
    @DisplayName("Should adjust stock count directly")
    void shouldAdjustStock() {
        WarehouseInventory inventory = WarehouseInventory.create(
                10L,
                100L,
                1L,
                501L,
                50,
                "corr-inv-5"
        );

        inventory.adjustStock(75, "corr-adj-1");
        assertEquals(75, inventory.getQuantity());
    }
}
