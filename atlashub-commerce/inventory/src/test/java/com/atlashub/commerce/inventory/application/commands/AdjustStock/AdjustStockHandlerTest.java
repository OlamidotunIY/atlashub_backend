package com.atlashub.commerce.inventory.application.commands.AdjustStock;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.domain.exceptions.InventoryNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockAdjustmentRepository;
import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdjustStockHandlerTest {

    private InventoryRepository inventoryRepository;
    private StockAdjustmentRepository stockAdjustmentRepository;
    private AdjustStockHandler handler;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        stockAdjustmentRepository = mock(StockAdjustmentRepository.class);
        handler = new AdjustStockHandler(inventoryRepository, stockAdjustmentRepository);
    }

    @Test
    @DisplayName("Should adjust stock and record adjustment successfully")
    void shouldAdjustStockSuccessfully() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 100L, null, 50, 10, 5);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockAdjustmentRepository.nextIdentity()).thenReturn(99L);

        AdjustStockCommand command = new AdjustStockCommand(1L, 60, AdjustmentReason.CORRECTION, 5L);
        Void result = handler.execute(command);

        assertNull(result);
        verify(inventoryRepository).save(inventory);
        verify(stockAdjustmentRepository).save(any(StockAdjustment.class));
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when inventory is absent")
    void shouldThrowWhenInventoryNotFound() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        AdjustStockCommand command = new AdjustStockCommand(1L, 60, AdjustmentReason.CORRECTION, 5L);
        assertThrows(InventoryNotFoundException.class, () -> handler.execute(command));
    }
}
