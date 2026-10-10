package com.atlashub.commerce.inventory.application.commands.InitiateStockCount;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitiateStockCountHandlerTest {

    private StockCountRepository stockCountRepository;
    private InventoryRepository inventoryRepository;
    private InitiateStockCountHandler handler;

    @BeforeEach
    void setUp() {
        stockCountRepository = mock(StockCountRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        handler = new InitiateStockCountHandler(stockCountRepository, inventoryRepository);
    }

    @Test
    @DisplayName("Should initiate stock count with snapshot of inventories")
    void shouldInitiateStockCountSuccessfully() {
        Inventory inv1 = Inventory.create(1L, 10L, 20L, 100L, null, 15, 10, 5);
        Inventory inv2 = Inventory.create(2L, 10L, 20L, 101L, null, 25, 10, 5);

        when(stockCountRepository.nextIdentity()).thenReturn(50L, 101L, 102L);
        when(inventoryRepository.findByOrganizationIdAndOutletId(10L, 20L)).thenReturn(List.of(inv1, inv2));

        InitiateStockCountCommand command = new InitiateStockCountCommand(10L, 20L, 3L);
        InitiateStockCountResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(50L, result.stockCountId());
        verify(stockCountRepository).save(any(StockCount.class));
    }
}
