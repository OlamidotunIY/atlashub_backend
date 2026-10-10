package com.atlashub.commerce.inventory.application.queries.GetStockSummary;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetStockSummaryHandlerTest {

    private InventoryRepository inventoryRepository;
    private GetStockSummaryHandler handler;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new GetStockSummaryHandler(inventoryRepository);
    }

    @Test
    @DisplayName("Should calculate stock summary metrics correctly")
    void shouldCalculateStockSummaryCorrectly() {
        Inventory inv1 = Inventory.create(1L, 10L, 20L, 100L, null, 50, 10, 5);
        Inventory inv2 = Inventory.create(2L, 10L, 20L, 101L, null, 8, 10, 5); // low stock
        Inventory inv3 = Inventory.create(3L, 10L, 20L, 102L, null, 0, 10, 5); // out of stock and low stock

        when(inventoryRepository.findByOrganizationIdAndOutletId(10L, 20L))
                .thenReturn(List.of(inv1, inv2, inv3));

        GetStockSummaryQuery query = new GetStockSummaryQuery(10L, 20L);
        StockSummaryResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(20L, result.outletId());
        assertEquals(3, result.totalSkus());
        assertEquals(58, result.totalQuantity());
        assertEquals(2, result.lowStockCount());
        assertEquals(1, result.outOfStockCount());
    }
}
