package com.atlashub.commerce.inventory.application.commands.ReconcileStockCount;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.exceptions.StockCountNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockAdjustmentRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReconcileStockCountHandlerTest {

    private StockCountRepository stockCountRepository;
    private InventoryRepository inventoryRepository;
    private StockAdjustmentRepository stockAdjustmentRepository;
    private ReconcileStockCountHandler handler;

    @BeforeEach
    void setUp() {
        stockCountRepository = mock(StockCountRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        stockAdjustmentRepository = mock(StockAdjustmentRepository.class);
        handler = new ReconcileStockCountHandler(stockCountRepository, inventoryRepository, stockAdjustmentRepository);
    }

    @Test
    @DisplayName("Should reconcile stock count and create adjustments for variances")
    void shouldReconcileStockCountAndAdjustVariance() {
        StockCount stockCount = StockCount.create(1L, 10L, 20L);
        stockCount.addItem(100L, 5L, 500L, 20);

        Inventory inventory = Inventory.create(5L, 10L, 20L, 500L, null, 20, 10, 5);

        when(stockCountRepository.findById(1L)).thenReturn(Optional.of(stockCount));
        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));
        when(stockAdjustmentRepository.nextIdentity()).thenReturn(999L);

        ReconcileStockCountCommand command = new ReconcileStockCountCommand(
                1L,
                List.of(new CountedItemDto(5L, 18)),
                2L
        );

        Void result = handler.execute(command);
        assertNull(result);

        verify(stockCountRepository).save(stockCount);
        verify(inventoryRepository).save(inventory);
        verify(stockAdjustmentRepository).save(any(StockAdjustment.class));
    }

    @Test
    @DisplayName("Should throw StockCountNotFoundException when stock count is missing")
    void shouldThrowWhenStockCountNotFound() {
        when(stockCountRepository.findById(1L)).thenReturn(Optional.empty());

        ReconcileStockCountCommand command = new ReconcileStockCountCommand(1L, List.of(), 2L);
        assertThrows(StockCountNotFoundException.class, () -> handler.execute(command));
    }
}
