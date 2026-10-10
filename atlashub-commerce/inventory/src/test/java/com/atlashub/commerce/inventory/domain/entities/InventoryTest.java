package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.LowStockResolvedEvent;
import com.atlashub.commerce.inventory.domain.events.StockAdjustedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InsufficientStockException;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.shared.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    @Test
    @DisplayName("Should create inventory with initial values")
    void create_validInventory_initializesCorrectly() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 100, 15, 5);

        assertEquals(1L, inventory.getId());
        assertEquals(10L, inventory.getOrganizationId());
        assertEquals(20L, inventory.getOutletId());
        assertEquals(30L, inventory.getProductId());
        assertEquals(100, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(15, inventory.getReorderLevel());
        assertEquals(5, inventory.getSafeStock());
        assertFalse(inventory.isLowStock());
    }

    @Test
    @DisplayName("Should throw exception when creating inventory with negative quantity")
    void create_negativeQuantity_throwsException() {
        assertThrows(InvalidInventoryOperationException.class, () ->
                Inventory.create(1L, 10L, 20L, 30L, null, -5, 10, 5)
        );
    }

    @Test
    @DisplayName("Should reserve stock when available")
    void reserveStock_validQuantity_incrementsReserved() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 5);

        inventory.reserveStock(20);

        assertEquals(50, inventory.getQuantity());
        assertEquals(20, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when reserving more than available")
    void reserveStock_insufficientStock_throwsInsufficientStockException() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 10, 5, 2);

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () ->
                inventory.reserveStock(15)
        );
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    @DisplayName("Should release reserved stock")
    void releaseReservedStock_validQuantity_decrementsReserved() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 5);
        inventory.reserveStock(20);

        inventory.releaseReservedStock(10);

        assertEquals(10, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should throw exception when releasing more reserved stock than held")
    void releaseReservedStock_moreThanReserved_throwsException() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 5);
        inventory.reserveStock(10);

        assertThrows(InvalidInventoryOperationException.class, () ->
                inventory.releaseReservedStock(15)
        );
    }

    @Test
    @DisplayName("Should deduct reserved stock")
    void deductStock_validQuantity_decrementsBoth() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 5);
        inventory.reserveStock(20);

        inventory.deductStock(15);

        assertEquals(35, inventory.getQuantity());
        assertEquals(5, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should throw exception when deducting unreserved stock")
    void deductStock_unreserved_throwsException() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 5);
        inventory.reserveStock(5);

        assertThrows(InvalidInventoryOperationException.class, () ->
                inventory.deductStock(10)
        );
    }

    @Test
    @DisplayName("Should add stock and emit LowStockResolvedEvent when crossing reorder level")
    void addStock_resolvesLowStock_emitsEvent() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 5, 10, 2);
        assertTrue(inventory.isLowStock());

        inventory.addStock(10);

        assertEquals(15, inventory.getQuantity());
        assertFalse(inventory.isLowStock());
        List<DomainEvent<?>> events1 = inventory.pullDomainEvents();
        assertEquals(1, events1.size());
        assertTrue(events1.get(0) instanceof LowStockResolvedEvent);
    }

    @Test
    @DisplayName("Should adjust stock and emit StockAdjustedEvent")
    void adjust_validQuantity_emitsStockAdjustedEvent() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 2);

        inventory.adjust(40, AdjustmentReason.DAMAGE, 999L);

        assertEquals(40, inventory.getQuantity());
        List<DomainEvent<?>> events2 = inventory.pullDomainEvents();
        assertEquals(1, events2.size());
        assertTrue(events2.get(0) instanceof StockAdjustedEvent);

        StockAdjustedEvent event = (StockAdjustedEvent) events2.get(0);
        assertEquals(50, event.payload().previousQty());
        assertEquals(40, event.payload().newQty());
        assertEquals(AdjustmentReason.DAMAGE, event.payload().reason());
        assertEquals(999L, event.payload().adjustedBy());
    }

    @Test
    @DisplayName("Should throw exception when adjusting to negative quantity")
    void adjust_negativeQuantity_throwsException() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 30L, null, 50, 10, 2);

        assertThrows(InvalidInventoryOperationException.class, () ->
                inventory.adjust(-5, AdjustmentReason.THEFT, 999L)
        );
    }
}
