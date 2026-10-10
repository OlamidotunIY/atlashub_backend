package com.atlashub.commerce.inventory.application.queries.ListLowStockProducts;

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

class ListLowStockProductsHandlerTest {

    private InventoryRepository inventoryRepository;
    private ListLowStockProductsHandler handler;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ListLowStockProductsHandler(inventoryRepository);
    }

    @Test
    @DisplayName("Should return list of low stock products")
    void shouldReturnLowStockProducts() {
        Inventory inv = Inventory.create(1L, 10L, 20L, 100L, null, 4, 10, 5);
        when(inventoryRepository.findLowStock(10L, 20L)).thenReturn(List.of(inv));

        ListLowStockProductsQuery query = new ListLowStockProductsQuery(10L, 20L);
        List<LowStockResult> results = handler.execute(query);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).id());
        assertEquals(4, results.get(0).quantity());
    }
}
